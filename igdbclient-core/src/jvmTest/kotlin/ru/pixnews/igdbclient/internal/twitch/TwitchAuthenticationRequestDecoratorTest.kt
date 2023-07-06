/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.internal.twitch

import io.kotest.matchers.collections.shouldMatchInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.BufferedSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery
import ru.pixnews.igdbclient.auth.model.TwitchToken
import ru.pixnews.igdbclient.auth.twitch.InMemoryTwitchTokenStorage
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenStorage
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse.Message
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.twitch.TwitchAuthenticationRequestDecorator.Companion.MAX_COMMIT_FRESH_TOKEN_ATTEMPTS
import ru.pixnews.igdbclient.library.test.MainCoroutineExtension
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import java.time.Instant
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

internal class TwitchAuthenticationRequestDecoratorTest {
    private val logger = Logger.getLogger("TwitchAuthenticationRequestDecoratorTest")

    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()

    @Test
    fun `Auth decorator with valid token should use correct executor`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment()

        val result = authDecorator.invoke() as? Success<String>

        result?.value shouldBe "Test Response"
        authDecorator.tokenFetcher.invokeCount shouldBe 0
        authDecorator.igdbExecutor.invokeCount shouldBe 1
        authDecorator.igdbExecutorFactory.invokeArgs.shouldMatchInOrder(
            { igdbAuthToken ->
                checkNotNull(igdbAuthToken).apply {
                    clientId shouldBe "test_client_id"
                    token shouldBe "testValidToken1"
                }
            },
        )
    }

    @Test
    fun `Auth decorator should reuse existing executor`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment()

        authDecorator("endpoint") as? Success<String>
        authDecorator("endpoint2") as? Success<String>

        authDecorator.igdbExecutorFactory.invokeCount shouldBe 1L
        authDecorator.igdbExecutor.invokeCount shouldBe 2L
    }

    @Test
    fun `Auth decorator should call token fetcher if has no token, HTTP 200 response`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
        )

        val result = authDecorator() as? Success<String>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        result?.value shouldBe "Test Response"
        authDecorator.tokenFetcher.invokeCount shouldBe 1
        authDecorator.igdbExecutor.invokeCount shouldBe 1
        authDecorator.igdbExecutorFactory.invokeArgs.shouldMatchInOrder(
            { igdbAuthToken ->
                checkNotNull(igdbAuthToken).apply {
                    clientId shouldBe "test_client_id"
                    token shouldBe "testValidToken2"
                }
            },
        )
        tokenInStorage shouldBe validToken2Payload
    }

    @Test
    fun `Auth decorator should fetch new token on non-parsable token in storage`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(
                TwitchTokenPayload(
                    payload = "Non-parsable token payload".encodeToByteArray(),
                ),
            ),
        )

        val result = authDecorator() as? Success<String>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        result?.value shouldBe "Test Response"
        authDecorator.tokenFetcher.invokeCount shouldBe 1
        tokenInStorage shouldBe validToken2Payload
    }

    @Test
    fun `Auth decorator should return error when token fetcher fails`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, _ ->
                Failure.HttpFailure(403, "Invalid credentials", null, null, null)
            },
        )

        val result = authDecorator()
        result shouldBe instanceOf<Failure.HttpFailure<*>>()

        val failure = result as Failure.HttpFailure<IgdbHttpErrorResponse>
        failure.httpCode shouldBe 403
        failure.httpMessage shouldBe "Invalid credentials"
        authDecorator.tokenFetcher.invokeCount shouldBe 1
        authDecorator.igdbExecutor.invokeCount shouldBe 0
    }

    @Test
    fun `Auth decorator should return error when cannot update token in storage`() = coroutinesExt.runTest {
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = object : TwitchTokenStorage {
                override suspend fun getToken(): TwitchTokenPayload = TwitchTokenPayload.NO_TOKEN
                override suspend fun updateToken(oldToken: TwitchTokenPayload, newToken: TwitchTokenPayload): Boolean {
                    return false
                }
            },
        )
        val result = authDecorator()
        result shouldBe instanceOf<Failure.UnknownFailure>()
        authDecorator.tokenFetcher.invokeCount shouldBe MAX_COMMIT_FRESH_TOKEN_ATTEMPTS
        authDecorator.igdbExecutor.invokeCount shouldBe 0
    }

    @Test
    fun `Auth decorator should not fetch token more that maxRequestRetries times`() = coroutinesExt.runTest {
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
        result shouldBe instanceOf<Failure.ApiFailure>()
        authDecorator.tokenFetcher.invokeCount shouldBe 2
        authDecorator.igdbExecutor.invokeCount shouldBe 0
    }

    @Test
    fun `Auth decorator should request new token when old token is expired`() = coroutinesExt.runTest {
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

        result?.value shouldBe "Test Response"
        tokenInStorage shouldBe validToken2Payload
        authDecorator.tokenFetcher.invokeCount shouldBe 1
        authDecorator.igdbExecutor.invokeCount shouldBe 2
        authDecorator.igdbExecutorFactory.invokeArgs.filterNotNull().shouldMatchInOrder(
            {
                it.clientId shouldBe "test_client_id"
                it.token shouldBe "testValidToken1"
            },
            {
                it.clientId shouldBe "test_client_id"
                it.token shouldBe "testValidToken2"
            },
        )
    }

    @Test
    fun `Auth decorator should return last response when can not fetch valid token`() = coroutinesExt.runTest {
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

        val result = authDecorator.invoke() as? HttpFailure<IgdbHttpErrorResponse>
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        result?.httpCode shouldBe 401
        result?.response shouldBe testIgdbResponse
        tokenInStorage shouldBe TwitchTokenPayload.NO_TOKEN
        authDecorator.igdbExecutor.invokeCount shouldBe 3
    }

    @Test
    fun `Auth decorator should not throw exceptions on cancelling while receiving token`() = coroutinesExt.runTest {
        val authTokenProviderLatch = Job()
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, _ ->
                authTokenProviderLatch.complete()
                logger.info("awaiting cancellation in auth token provider")
                awaitCancellation()
            },
        )

        val request = backgroundScope.launch {
            authDecorator.invoke()
            error("This code should not be reachable since the request is canceled")
        }
        authTokenProviderLatch.join()
        request.cancelAndJoin()
    }

    @Test
    fun `Auth decorator should not throw exceptions on cancelling while receiving response`() = coroutinesExt.runTest {
        val igdbExecutorLatch = Job()
        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(validToken1Payload),
            igdbExecutor = TracingRequestExecutor { _, _ ->
                igdbExecutorLatch.complete()
                awaitCancellation()
            },
        )

        val request = backgroundScope.launch {
            authDecorator.invoke()
            error("This code should not be reachable since the request is canceled")
        }
        igdbExecutorLatch.join()
        request.cancelAndJoin()
    }

    @Test
    fun `Auth decorator should not make more than one token request at a time`() = coroutinesExt.runTest {
        val firstRequestLaunchedLatch = Job().apply {
            invokeOnCompletion { logger.info("First request: launch complete, waiting for subsequent requests to run") }
        }
        val subsequentRequestsLaunchedLatch = Job().apply {
            invokeOnCompletion { logger.info("Subsequent requests: launch complete") }
        }

        val authDecorator = TwitchDecoratorTestEnvironment(
            tokenStorage = InMemoryTwitchTokenStorage(TwitchTokenPayload.NO_TOKEN),
            tokenFetcher = TracingTwitchTokenFetcher { _, requestNo ->
                when (requestNo) {
                    1L -> {
                        firstRequestLaunchedLatch.complete()
                        subsequentRequestsLaunchedLatch.join()
                        logger.info("First fetch token request: return validToken1 after a short delay")
                        delay(50)
                        Success(validToken1)
                    }

                    else -> {
                        logger.info("Subsequent fetch token request: return validToken2")
                        Success(validToken2)
                    }
                }
            },
        )

        val firstRequestJob = backgroundScope.async {
            authDecorator()
        }.apply {
            invokeOnCompletion { logger.info("First request complete") }
        }

        firstRequestLaunchedLatch.join()
        val subsequentRequests = (1 until 5).map { requestNo ->
            backgroundScope.async {
                authDecorator()
            }.apply {
                invokeOnCompletion { logger.info("Subsequent request $requestNo complete") }
            }
        }
        subsequentRequestsLaunchedLatch.complete()

        val firstResponse = firstRequestJob.await() as? Success
        subsequentRequests.awaitAll()
        val tokenInStorage = authDecorator.tokenStorage.getToken()

        firstResponse?.value shouldBe "Test Response"
        tokenInStorage shouldBe validToken1Payload
        authDecorator.tokenFetcher.invokeCount shouldBe 1
        authDecorator.igdbExecutor.invokeCount shouldBe 5
        authDecorator.igdbExecutorFactory.invokeArgs.shouldMatchInOrder(
            { authToken ->
                checkNotNull(authToken).apply {
                    clientId shouldBe "test_client_id"
                    token shouldBe "testValidToken1"
                }
            },
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
            requestExecutorFactory = igdbExecutorFactory,
            maxRequestRetries = maxRequestRetries,
        )

        suspend operator fun invoke(
            endpoint: String = "endpoint",
            query: ApicalypseQuery = apicalypseQuery { },
            successResponseParser: (ApicalypseQuery, BufferedSource) -> String = { _, _ -> "" },
        ): IgdbResult<String, IgdbHttpErrorResponse> = authDecorator(
            ApicalypsePostRequest(endpoint, query, successResponseParser),
        )
    }

    @Suppress("MaxLineLength", "ParameterWrapping")
    private class TracingTwitchTokenFetcher(
        val delegate: suspend (credentials: TwitchCredentials, requestNo: Long) -> IgdbResult<TwitchToken, TwitchErrorResponse>,
    ) : TwitchTokenFetcher {
        private val _invokeCount: AtomicLong = AtomicLong(0)
        val invokeCount: Long
            get() = _invokeCount.get()

        override suspend fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse> =
            delegate(credentials, _invokeCount.incrementAndGet())
    }

    private class TracingRequestExecutorFactory(
        private val delegate: (IgdbAuthToken?, Long) -> RequestExecutor,
    ) : (IgdbAuthToken?) -> RequestExecutor {
        val invokeArgs: MutableList<IgdbAuthToken?> = Collections.synchronizedList(mutableListOf())
        private var _invokeCount: Long = 0
        val invokeCount: Long
            get() = synchronized(invokeArgs) { _invokeCount }

        override fun invoke(token: IgdbAuthToken?): RequestExecutor {
            val invokeNumber = synchronized(invokeArgs) {
                invokeArgs.add(token)
                ++_invokeCount
            }
            return delegate.invoke(token, invokeNumber)
        }
    }

    companion object {
        private val validToken1: TwitchToken = TwitchToken(
            access_token = "testValidToken1",
            expires_in = 5035365,
            token_type = "bearer",
            receive_timestamp = Instant.ofEpochMilli(1_686_895_955_123),
        )
        private val validToken1Payload: TwitchTokenPayload = TwitchTokenPayload(validToken1.encode())
        private val validToken2: TwitchToken = TwitchToken(
            access_token = "testValidToken2",
            expires_in = 5035365,
            token_type = "bearer",
            receive_timestamp = Instant.ofEpochMilli(1_686_896_255_123),
        )
        private val validToken2Payload: TwitchTokenPayload = TwitchTokenPayload(validToken2.encode())

        internal data class TestCredentials(
            override val clientId: String = "test_client_id",
            override val clientSecret: String = "test_client_secret",
        ) : TwitchCredentials
    }
}
