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
package ru.pixnews.igdbclient.internal

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery
import ru.pixnews.igdbclient.auth.model.TwitchToken
import ru.pixnews.igdbclient.dsl.IgdbClientConfigBlock
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.library.test.MainCoroutineExtension
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class RootExecutorFactoryTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()

    @Test
    fun `Executor should not make requests to the twitch server if auth not configured`() = coroutinesExt.runTest {
        val mockHttpClient = MockIgdbHttpClient()
        val config = IgdbClientConfigBlock<Nothing>().apply {}.build()

        val executor = buildRequestExecutor(config, mockHttpClient)

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        (response as? Success<String>)?.value shouldBe success200Response.value
    }

    @Test
    fun `Executor should make requests to the twitch server if twitch auth is configured`() = coroutinesExt.runTest {
        val twitchAuthRequested = AtomicBoolean(false)
        val executor = buildRequestExecutor(
            config = IgdbClientConfigBlock<Nothing>().apply {
                twitchAuth {
                    clientId = "testClientId"
                    clientSecret = "testClientSecret"
                }
            }.build(),
            igdbHttpClient = MockIgdbHttpClient(
                twitchTokenFetcher = { _ ->
                    twitchAuthRequested.set(true)
                    Success(TwitchToken("testAccessToken"))
                },
            ),
        )

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        twitchAuthRequested.get() shouldBe true
        (response as? Success<String>)?.value shouldBe success200Response.value
    }

    @Test
    fun `Executor should retry requests on 429 error by default`() = coroutinesExt.runTest {
        val requestExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L, 2L -> error429Response
                else -> success200Response
            }
        }
        val mockHttpClient = MockIgdbHttpClient(
            mockFactory = { _ -> requestExecutor },
        )
        val config = IgdbClientConfigBlock<Nothing>().apply {}.build()

        val executor = buildRequestExecutor(config, mockHttpClient)

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        response.shouldBeInstanceOf<Success<String>>()
        response.value shouldBe success200Response.value
    }

    @Test
    fun `Executor should not retry requests on 429 error when retries disabled`() = coroutinesExt.runTest {
        val childRequestExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L, 2L -> error429Response
                else -> success200Response
            }
        }
        val mockHttpClient = MockIgdbHttpClient(
            mockFactory = { _ -> childRequestExecutor },
        )
        val config = IgdbClientConfigBlock<Nothing>().apply {
            retryPolicy {
                enabled = false
            }
        }.build()

        val executor = buildRequestExecutor(config, mockHttpClient)

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        response.shouldBeInstanceOf<HttpFailure<IgdbHttpErrorResponse>>()
        response.httpCode shouldBe 429
    }

    @Test
    fun `Custom retry policy configuration test`() = coroutinesExt.runTest {
        val childRequestExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                in 1L..10L -> error429Response
                else -> success200Response
            }
        }
        val mockHttpClient = MockIgdbHttpClient(
            mockFactory = { _ -> childRequestExecutor },
        )
        val config = IgdbClientConfigBlock<Nothing>().apply {
            retryPolicy {
                maxRequestRetries = 9
                initialDelay = 50.milliseconds
                factor = 1f
                delayRange = 100.milliseconds..1.minutes
                jitterFactor = 0f
            }
        }.build()

        val executor = buildRequestExecutor(config, mockHttpClient)

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        response.shouldBeInstanceOf<HttpFailure<IgdbHttpErrorResponse>>()
        response.httpCode shouldBe 429
        childRequestExecutor.invokeCount shouldBe 10
        testScheduler.currentTime shouldBe 900
    }

    class MockIgdbHttpClient(
        mockFactory: (IgdbAuthToken?) -> RequestExecutor = { _ ->
            TracingRequestExecutor { _, _ -> success200Response }
        },
        twitchTokenFetcher: (TwitchCredentials) -> IgdbResult<TwitchToken, TwitchErrorResponse> = { _ ->
            error("Requests to the twitch token fetcher are not allowed")
        },
    ) : IgdbHttpClient {
        private val _tokenFetcherFactory: () -> TwitchTokenFetcher = {
            object : TwitchTokenFetcher {
                override suspend fun invoke(
                    credentials: TwitchCredentials,
                ): IgdbResult<TwitchToken, TwitchErrorResponse> = twitchTokenFetcher(credentials)
            }
        }
        override val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor = mockFactory
        override val twitchTokenFetcherFactory: () -> TwitchTokenFetcher = _tokenFetcherFactory
    }

    internal companion object {
        val testApicalypseRequest: ApicalypsePostRequest<String> = ApicalypsePostRequest(
            path = "endpoint",
            query = apicalypseQuery { },
            successResponseParser = { _, _ -> "" },
        )
        val success200Response: Success<String> = Success("Mock Success Response")
        val error429Response: HttpFailure<IgdbHttpErrorResponse> = HttpFailure(
            httpCode = 429,
            httpMessage = "429 Too Many Requests",
            rawResponseBody = null,
            rawResponseHeaders = null,
            response = null,
        )
    }
}
