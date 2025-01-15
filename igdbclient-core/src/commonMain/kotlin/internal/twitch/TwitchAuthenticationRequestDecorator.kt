/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.twitch

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.IgdbResult.Failure
import at.released.igdbclient.IgdbResult.Failure.ApiFailure
import at.released.igdbclient.IgdbResult.Failure.HttpFailure
import at.released.igdbclient.IgdbResult.Failure.NetworkFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import at.released.igdbclient.IgdbResult.Success
import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.auth.twitch.TwitchTokenPayload
import at.released.igdbclient.auth.twitch.TwitchTokenStorage
import at.released.igdbclient.error.IgdbException
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.error.IgdbHttpErrorResponse.Message
import at.released.igdbclient.internal.IgdbRequest
import at.released.igdbclient.internal.RequestExecutor
import at.released.igdbclient.internal.model.IgdbAuthToken
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.model.TwitchToken.Companion.encode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.IOException

@InternalIgdbClientApi
internal class TwitchAuthenticationRequestDecorator(
    private val credentials: TwitchCredentials,
    private val tokenStorage: TwitchTokenStorage,
    private val maxRequestRetries: Int = 3,
    private val twitchTokenFetcher: TwitchTokenFetcher,
    requestExecutorFactory: (token: IgdbAuthToken?) -> RequestExecutor,
) : RequestExecutor {
    private val fetchTokenMutex = Mutex()
    private val requestExecutorProvider = CachedRequestExecutor(credentials, requestExecutorFactory)

    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        return SingleRequestExecutor<T>(request).execute()
    }

    private inner class SingleRequestExecutor<out T : Any>(
        private val request: IgdbRequest,
        private val maxFetchTokenAttempts: Int = this@TwitchAuthenticationRequestDecorator.maxRequestRetries,
        private val maxRequestRetries: Int = this@TwitchAuthenticationRequestDecorator.maxRequestRetries,
    ) {
        private var requests = 0
        private var fetchTokenRequests = 0

        suspend fun execute(): IgdbResult<T, IgdbHttpErrorResponse> {
            var lastResponse: IgdbResult<T, IgdbHttpErrorResponse> = UnknownFailure(null)
            while (requests < maxRequestRetries) {
                val getTokenResult = fetchTokenMutex.withLock {
                    getFreshToken()
                }
                val (tokenPayload, executor) = when (getTokenResult) {
                    is Success -> getTokenResult.value
                    is Failure -> return getTokenResult
                }

                lastResponse = executor(request)

                if (!lastResponse.is401UnauthorizedFailure()) {
                    return lastResponse
                }

                // Invalidate token
                tokenStorage.updateToken(tokenPayload, TwitchTokenPayload.NO_TOKEN)
                requests += 1
            }
            return lastResponse
        }

        @Suppress("ReturnCount")
        private suspend fun getFreshToken(): IgdbResult<TokenPayloadWithExecutor, IgdbHttpErrorResponse> {
            repeat(MAX_COMMIT_FRESH_TOKEN_ATTEMPTS) {
                val storedTokenPayload = tokenStorage.getToken()
                requestExecutorProvider(storedTokenPayload)?.let {
                    return Success(TokenPayloadWithExecutor(storedTokenPayload, it))
                }

                if (fetchTokenRequests >= maxFetchTokenAttempts) {
                    return ApiFailure(
                        RequestTokenException(
                            "Number of attempts to get a fresh token exceeded the limit of $maxFetchTokenAttempts",
                        ),
                    )
                }

                val fetchTokenResult = twitchTokenFetcher(credentials).asIgdbResult()
                if (fetchTokenResult is Failure) {
                    return fetchTokenResult
                }
                fetchTokenRequests += 1

                val fetchedTokenPayload = (fetchTokenResult as Success<TwitchTokenPayload>).value
                tokenStorage.updateToken(storedTokenPayload, fetchedTokenPayload)
            }

            return UnknownFailure(RequestTokenException("Failed to fetch token"))
        }
    }

    internal class RequestTokenException(message: String?) : IgdbException(message)

    private class CachedRequestExecutor(
        private val twitchCredentials: TwitchCredentials,
        private val requestExecutorFactory: (token: IgdbAuthToken?) -> RequestExecutor,
    ) {
        private val lock: Mutex = Mutex()
        private var cachedTokenPayload: TwitchTokenPayload = TwitchTokenPayload.NO_TOKEN
        private var exeutor: RequestExecutor = dummyRequestExecutor

        suspend operator fun invoke(tokenPayload: TwitchTokenPayload): RequestExecutor? = lock.withLock {
            if (tokenPayload == cachedTokenPayload && exeutor != dummyRequestExecutor) {
                return exeutor
            }

            val twitchToken = tokenPayload.deserializeToken()?.toIgdbToken(twitchCredentials)
            return if (twitchToken != null) {
                return requestExecutorFactory(twitchToken).also {
                    exeutor = it
                    cachedTokenPayload = tokenPayload
                }
            } else {
                null
            }
        }
    }

    private class TokenPayloadWithExecutor(
        val payload: TwitchTokenPayload,
        val executor: RequestExecutor,
    ) {
        operator fun component1() = payload
        operator fun component2() = executor
    }

    internal companion object {
        internal const val MAX_COMMIT_FRESH_TOKEN_ATTEMPTS = 3
        private val dummyRequestExecutor = object : RequestExecutor {
            override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
                return UnknownFailure(null)
            }
        }

        @Suppress("MagicNumber")
        private fun IgdbResult<*, *>.is401UnauthorizedFailure() = this is HttpFailure<*> &&
                this.httpCode == 401

        private fun TwitchTokenPayload.deserializeToken(): TwitchToken? {
            if (isEmpty()) {
                return null
            }
            return try {
                val token = TwitchToken.decode(payload)
                if (token.accessToken.isNotEmpty()) token else null
            } catch (@Suppress("SwallowedException") exception: IOException) {
                null
            }
        }

        private fun TwitchToken.toIgdbToken(credentials: TwitchCredentials): IgdbAuthToken = object : IgdbAuthToken {
            override val clientId: String = credentials.clientId
            override val token: String = this@toIgdbToken.accessToken
        }

        private fun IgdbResult<TwitchToken, TwitchErrorResponse>.asIgdbResult():
                IgdbResult<TwitchTokenPayload, IgdbHttpErrorResponse> = when (this) {
            is Success -> Success(TwitchTokenPayload(payload = value.encode()))
            is ApiFailure -> this
            is NetworkFailure -> this
            is UnknownFailure -> this
            is UnknownHttpCodeFailure -> this
            is HttpFailure -> HttpFailure(
                httpCode = httpCode,
                httpMessage = httpMessage,
                response = response?.let { twitchResponse ->
                    IgdbHttpErrorResponse(
                        messages = listOf(
                            Message(
                                status = twitchResponse.status,
                                title = twitchResponse.message,
                                cause = "Twitch error",
                            ),
                        ),
                    )
                },
                rawResponseBody = this@asIgdbResult.rawResponseBody,
                rawResponseHeaders = this@asIgdbResult.rawResponseHeaders,
            )
        }
    }
}
