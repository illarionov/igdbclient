/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlinx.coroutines.test.runTest
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.dsl.IgdbClientConfigBlock
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class RootExecutorFactoryTest {
    @Test
    fun executor_should_not_make_requests_to_the_twitch_server_if_auth_not_configured() = runTest {
        val mockHttpClient = MockIgdbHttpClient()
        val config = IgdbClientConfigBlock<Nothing>().apply {}.build()

        val executor = buildRequestExecutor(config, mockHttpClient)

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        assertThat(response)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo(success200Response.value)
    }

    @Test
    fun executor_should_make_requests_to_the_twitch_server_if_twitch_auth_is_configured() = runTest {
        var twitchAuthRequested = false
        val executor = buildRequestExecutor(
            config = IgdbClientConfigBlock<Nothing>().apply {
                twitchAuth {
                    clientId = "testClientId"
                    clientSecret = "testClientSecret"
                }
            }.build(),
            igdbHttpClient = MockIgdbHttpClient(
                twitchTokenFetcher = { _ ->
                    twitchAuthRequested = true
                    IgdbResult.Success(TwitchToken("testAccessToken"))
                },
            ),
        )

        val response: IgdbResult<String, IgdbHttpErrorResponse> = executor.invoke(testApicalypseRequest)

        assertThat(twitchAuthRequested).isTrue()
        assertThat(response)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo(success200Response.value)
    }

    @Test
    fun executor_should_retry_requests_on_429_error_by_default() = runTest {
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

        assertThat(response)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo(success200Response.value)
    }

    @Test
    fun executor_should_not_retry_requests_on_429_error_when_retries_disabled() = runTest {
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

        assertThat(response)
            .isInstanceOf<IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse>>()
            .prop(IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse>::httpCode).isEqualTo(429)
    }

    @Test
    fun custom_retry_policy_configuration_test() = runTest {
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

        assertThat(response)
            .isInstanceOf<IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse>>()
            .prop(IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse>::httpCode).isEqualTo(429)

        assertThat(childRequestExecutor.invokeCount).isEqualTo(10)
        assertThat(testScheduler.currentTime).isEqualTo(900)
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
        val testApicalypseRequest: IgdbRequest.ApicalypsePostRequest<String> = IgdbRequest.ApicalypsePostRequest(
            path = "endpoint",
            query = apicalypseQuery { },
            successResponseParser = { _ -> "" },
        )
        val success200Response: IgdbResult.Success<String> = IgdbResult.Success("Mock Success Response")
        val error429Response: IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse> = IgdbResult.Failure.HttpFailure(
            httpCode = 429,
            httpMessage = "429 Too Many Requests",
            rawResponseBody = null,
            rawResponseHeaders = null,
            response = null,
        )
    }
}
