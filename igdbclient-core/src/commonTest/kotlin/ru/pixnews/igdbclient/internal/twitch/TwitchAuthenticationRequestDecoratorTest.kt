/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal.twitch

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isZero
import assertk.assertions.prop
import com.squareup.wire.ofEpochSecond
import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.auth.twitch.InMemoryTwitchTokenStorage
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenStorage
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse.Message
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.model.TwitchToken.Companion.encode
import ru.pixnews.igdbclient.internal.twitch.TwitchAuthenticationRequestDecorator.Companion.MAX_COMMIT_FRESH_TOKEN_ATTEMPTS
import ru.pixnews.igdbclient.library.test.TestingLoggers
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import kotlin.test.Test

class TwitchAuthenticationRequestDecoratorTest {
    private val logger = TestingLoggers.consoleLogger.withTag("TwitchAuthenticationRequestDecoratorTest")

    @Test
    fun auth_decorator_with_valid_token_should_use_correct_executor() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment()

        val result = authDecorator() as? Success<String>

        assertThat(result?.value).isEqualTo("Test Response")
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(0)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(1)

        assertThat(authDecorator.igdbExecutorFactory.invokeArgs).hasSingleItemOf(
            clientId = "test_client_id",
            token = "testValidToken1",
        )
    }

    @Test
    fun auth_decorator_should_reuse_existing_executor() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment()

        authDecorator("endpoint") as? Success<String>
        authDecorator("endpoint2") as? Success<String>

        assertThat(authDecorator.igdbExecutorFactory.invokeCount).isEqualTo(1L)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(2L)
    }

    @Test
    fun auth_decorator_should_call_token_fetcher_if_has_no_token_HTTP_200_response() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
        )

        val result = authDecorator() as? Success<String>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        assertThat(result?.value).isEqualTo("Test Response")
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(1)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(1)
        assertThat(authDecorator.igdbExecutorFactory.invokeArgs).all {
            hasSize(1)
            index(0)
                .isEqualTo(
                    clientId = "test_client_id",
                    token = "testValidToken2",
                )
        }
        assertThat(tokenInStorage).isEqualTo(validToken2Payload)
    }

    @Test
    fun auth_decorator_should_fetch_new_token_on_non_parsable_token_in_storage() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(
                TwitchTokenPayload(
                    payload = "Non-parsable token payload".encodeToByteArray(),
                ),
            ),
        )

        val result = authDecorator() as? Success<String>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        assertThat(result?.value).isEqualTo("Test Response")
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(1)
        assertThat(tokenInStorage).isEqualTo(validToken2Payload)
    }

    @Test
    fun auth_decorator_should_return_error_when_token_fetcher_fails() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, _ ->
                HttpFailure(403, "Invalid credentials", null, null, null)
                HttpFailure(403, "Invalid credentials", null, null, null)
            },
        )

        val result = authDecorator()
        assertThat(result)
            .isInstanceOf<HttpFailure<IgdbHttpErrorResponse>>()
            .all {
                prop(HttpFailure<*>::httpCode).isEqualTo(403)
                prop(HttpFailure<*>::httpMessage).isEqualTo("Invalid credentials")
            }
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(1)
        assertThat(authDecorator.igdbExecutor.invokeCount).isZero()
    }

    @Test
    fun auth_decorator_should_return_error_when_cannot_update_token_in_storage() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = object : TwitchTokenStorage {
                override suspend fun getToken(): TwitchTokenPayload = TwitchTokenPayload.NO_TOKEN
                override suspend fun updateToken(oldToken: TwitchTokenPayload, newToken: TwitchTokenPayload): Boolean {
                    return false
                }
            },
        )
        val result = authDecorator()

        assertThat(result).isInstanceOf<Failure.UnknownFailure>()
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(MAX_COMMIT_FRESH_TOKEN_ATTEMPTS.toLong())
        assertThat(authDecorator.igdbExecutor.invokeCount).isZero()
    }

    @Test
    fun auth_decorator_should_not_fetch_token_more_that_maxRequestRetries_times() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = object : TwitchTokenStorage {
                override suspend fun getToken(): TwitchTokenPayload = TwitchTokenPayload.NO_TOKEN
                override suspend fun updateToken(oldToken: TwitchTokenPayload, newToken: TwitchTokenPayload): Boolean {
                    return false
                }
            },
            maxRequestRetries = 2,
        )
        val result = authDecorator()
        assertThat(result).isInstanceOf<Failure.ApiFailure>()
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(2)
        assertThat(authDecorator.igdbExecutor.invokeCount).isZero()
    }

    @Test
    fun auth_decorator_should_request_new_token_when_old_token_is_expired() = runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(validToken1Payload),
            igdbExecutor = TracingRequestExecutor { _, requestNo ->
                when (requestNo) {
                    1L -> HttpFailure<IgdbHttpErrorResponse>(
                        httpCode = 401,
                        httpMessage = "Invalid authtoken",
                        response = null,
                        rawResponseHeaders = null,
                        rawResponseBody = null,
                    )

                    else -> Success("Test Response")
                }
            },
        )

        val result = authDecorator.invoke() as? Success<String>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        assertThat(result?.value).isEqualTo("Test Response")
        assertThat(tokenInStorage).isEqualTo(validToken2Payload)
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(1)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(2)
        assertThat(authDecorator.igdbExecutorFactory.invokeArgs.filterNotNull()).all {
            hasSize(2)
            index(0).isEqualTo(
                clientId = "test_client_id",
                token = "testValidToken1",
            )
            index(1).isEqualTo(
                clientId = "test_client_id",
                token = "testValidToken2",
            )
        }
    }

    @Test
    fun auth_decorator_should_return_last_response_when_can_not_fetch_valid_token() = runTest {
        val testIgdbResponse = IgdbHttpErrorResponse(
            listOf(Message(401, "Expired authtoken", null)),
        )
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(validToken1Payload),
            igdbExecutor = TracingRequestExecutor { _, _ ->
                HttpFailure(
                    401,
                    "Expired authtoken",
                    testIgdbResponse,
                    null,
                    null,
                )
            },
            maxRequestRetries = 3,
        )

        val result = authDecorator()
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        assertThat(result)
            .isInstanceOf<HttpFailure<IgdbHttpErrorResponse>>()
            .all {
                prop(HttpFailure<*>::httpCode).isEqualTo(401)
                prop(HttpFailure<*>::response).isEqualTo(testIgdbResponse)
            }

        assertThat(tokenInStorage).isEqualTo(TwitchTokenPayload.NO_TOKEN)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(3)
    }

    @Test
    fun auth_decorator_should_not_throw_exceptions_on_cancelling_while_receiving_token() = runTest {
        val authTokenProviderLatch = Job()
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, _ ->
                authTokenProviderLatch.complete()
                logger.i { "awaiting cancellation in auth token provider" }
                awaitCancellation()
            },
        )

        val request = backgroundScope.launch {
            authDecorator()
            error("This code should not be reachable since the request is canceled")
        }
        authTokenProviderLatch.join()
        request.cancelAndJoin()
    }

    @Test
    fun auth_decorator_should_not_throw_exceptions_on_cancelling_while_receiving_response() = runTest {
        val igdbExecutorLatch = Job()
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(validToken1Payload),
            igdbExecutor = TracingRequestExecutor { _, _ ->
                igdbExecutorLatch.complete()
                awaitCancellation()
            },
        )

        val request = backgroundScope.launch {
            authDecorator()
            error("This code should not be reachable since the request is canceled")
        }
        igdbExecutorLatch.join()
        request.cancelAndJoin()
    }

    @Test
    fun auth_decorator_should_not_make_more_than_one_token_request_at_a_time() = runTest {
        val firstRequestLaunchedLatch = Job().apply {
            invokeOnCompletion { logger.i { "First request: launch complete, waiting for subsequent requests to run" } }
        }
        val subsequentRequestsLaunchedLatch = Job().apply {
            invokeOnCompletion { logger.i { "Subsequent requests: launch complete" } }
        }

        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, requestNo ->
                when (requestNo) {
                    1L -> {
                        firstRequestLaunchedLatch.complete()
                        subsequentRequestsLaunchedLatch.join()
                        logger.i { "First fetch token request: return validToken1 after a short delay" }
                        delay(50)
                        Success(validToken1)
                    }

                    else -> {
                        logger.i { "Subsequent fetch token request: return validToken2" }
                        Success(validToken2)
                    }
                }
            },
        )

        val firstRequestJob = backgroundScope.async {
            authDecorator()
        }.apply {
            invokeOnCompletion { logger.i { "First request complete" } }
        }

        firstRequestLaunchedLatch.join()
        val subsequentRequests = (1 until 5).map { requestNo ->
            backgroundScope.async {
                authDecorator()
            }.apply {
                invokeOnCompletion { logger.i { "Subsequent request $requestNo complete" } }
            }
        }
        subsequentRequestsLaunchedLatch.complete()

        val firstResponse = firstRequestJob.await() as? Success
        subsequentRequests.awaitAll()
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        assertThat(firstResponse?.value).isEqualTo("Test Response")
        assertThat(tokenInStorage).isEqualTo(validToken1Payload)
        assertThat(authDecorator.tokenFetcher.invokeCount).isEqualTo(1)
        assertThat(authDecorator.igdbExecutor.invokeCount).isEqualTo(5)
        assertThat(authDecorator.igdbExecutorFactory.invokeArgs).hasSingleItemOf(
            clientId = "test_client_id",
            token = "testValidToken1",
        )
    }

    private class TwitchDecoratorTestEnvironment(
        val credentials: TwitchCredentials = TestCredentials(),
        val tokenStorage: TwitchTokenStorage = InMemoryTwitchTokenStorage(validToken1Payload),
        val tokenFetcher: TracingTwitchTokenFetcher = TracingTwitchTokenFetcher { _, _ -> Success(validToken2) },
        val igdbExecutor: TracingRequestExecutor = TracingRequestExecutor { _, _ ->
            Success("Test Response")
        },
        val igdbExecutorFactory: TracingRequestExecutorFactory = TracingRequestExecutorFactory { _, _ -> igdbExecutor },
        val maxRequestRetries: Int = 3,
    ) {
        val authDecorator = TwitchAuthenticationRequestDecorator(
            credentials = credentials,
            tokenStorage = tokenStorage,
            twitchTokenFetcher = tokenFetcher,
            requestExecutorFactory = igdbExecutorFactory::invoke,
            maxRequestRetries = maxRequestRetries,
        )

        suspend operator fun invoke(
            endpoint: String = "endpoint",
            successResponseParser: (BufferedSource) -> String = { _ -> "" },
        ): IgdbResult<String, IgdbHttpErrorResponse> = authDecorator(
            ApicalypsePostRequest(endpoint, apicalypseQuery { }, successResponseParser),
        )
    }

    @Suppress("MaxLineLength", "ParameterWrapping")
    private class TracingTwitchTokenFetcher(
        val delegate: suspend (credentials: TwitchCredentials, requestNo: Long) -> IgdbResult<TwitchToken, TwitchErrorResponse>,
    ) : TwitchTokenFetcher {
        private val _invokeCount: AtomicLong = atomic(0L)
        val invokeCount: Long
            get() = _invokeCount.value

        override suspend fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse> =
            delegate(credentials, _invokeCount.incrementAndGet())
    }

    private class TracingRequestExecutorFactory(
        private val delegate: (IgdbAuthToken?, Long) -> RequestExecutor,
    ) {
        private val lock: ReentrantLock = reentrantLock()
        private val _invokeArgs: MutableList<IgdbAuthToken?> = mutableListOf()
        private var _invokeCount: Long = 0

        val invokeCount: Long
            get() = lock.withLock { _invokeCount }

        val invokeArgs: List<IgdbAuthToken?>
            get() = lock.withLock {
                _invokeArgs.toMutableList()
            }

        fun invoke(token: IgdbAuthToken?): RequestExecutor {
            val invokeNumber = lock.withLock {
                _invokeArgs.add(token)
                ++_invokeCount
            }
            return delegate.invoke(token, invokeNumber)
        }
    }

    companion object {
        private val validToken1: TwitchToken = TwitchToken(
            accessToken = "testValidToken1",
            expiresIn = 5035365,
            tokenType = "bearer",
            receiveTimestamp = ofEpochSecond(1_686_895_955, 123_000_000),
        )
        private val validToken1Payload: TwitchTokenPayload = TwitchTokenPayload(validToken1.encode())
        private val validToken2: TwitchToken = TwitchToken(
            accessToken = "testValidToken2",
            expiresIn = 5035365,
            tokenType = "bearer",
            receiveTimestamp = ofEpochSecond(1_686_896_255, 123_000_000),
        )
        private val validToken2Payload: TwitchTokenPayload = TwitchTokenPayload(validToken2.encode())

        private fun Assert<IgdbAuthToken?>.isEqualTo(
            clientId: String,
            token: String,
        ): Unit = this.isNotNull().all {
            prop(IgdbAuthToken::clientId).isEqualTo(clientId)
            prop(IgdbAuthToken::token).isEqualTo(token)
        }

        private fun Assert<List<IgdbAuthToken?>>.hasSingleItemOf(
            clientId: String,
            token: String,
        ) = all {
            hasSize(1)
            index(0)
                .isEqualTo(
                    clientId = clientId,
                    token = token,
                )
        }

        internal data class TestCredentials(
            override val clientId: String = "test_client_id",
            override val clientSecret: String = "test_client_secret",
        ) : TwitchCredentials
    }
}
