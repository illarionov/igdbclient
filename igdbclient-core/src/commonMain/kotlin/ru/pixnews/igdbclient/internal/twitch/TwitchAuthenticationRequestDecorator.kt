/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.twitch

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.IOException
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure
import ru.pixnews.igdbclient.IgdbResult.Failure.ApiFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.NetworkFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenStorage
import ru.pixnews.igdbclient.error.IgdbException
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse.Message
import ru.pixnews.igdbclient.internal.IgdbRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.model.TwitchToken.Companion.encode

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
