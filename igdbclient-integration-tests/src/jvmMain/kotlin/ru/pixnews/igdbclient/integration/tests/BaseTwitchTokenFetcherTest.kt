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
@file:Suppress(
    "FunctionName",
    "KDOC_NO_EMPTY_TAGS",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "MagicNumber",
)

package ru.pixnews.igdbclient.integration.tests

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.MediaType
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout
import java.time.Instant

/**
 * Base class with integration tests running on different implementations of the TwitchTokenFetcher
 */
abstract class BaseTwitchTokenFetcherTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    abstract fun createTwitchTokenFetcher(
        baseUrl: String,
        userAgent: String?,
        tokenTimestampSource: () -> Long,
    ): TwitchTokenFetcher

    @Suppress("NewApi")
    @Test
    fun `Fetcher should correctly parse success 200 response`() = coroutinesExt.runTest {
        val fetcher = startMockServerCreateFetcher { request ->
            if (request.path == "/") createSuccessMockResponse() else null
        }

        val response = fetcher(TestCredentials()) as? IgdbResult.Success

        checkNotNull(response).value.run {
            access_token shouldBe "123"
            expires_in shouldBe 5066399
            token_type shouldBe "bearer"
            receive_timestamp shouldBe Instant.ofEpochMilli(1_686_895_955_123)
        }
    }

    @Test
    fun `Fetcher should send correct headers`() = coroutinesExt.runTest {
        val fetcher = startMockServerCreateFetcher { createSuccessMockResponse() }

        fetcher(TestCredentials())

        server.takeRequestWithTimeout().run {
            headers.values("Accept") shouldBe listOf("application/json")
            headers.values("User-Agent") shouldBe listOf("Test user agent")
            body.readByteString().utf8().split("&")
                .shouldContainExactlyInAnyOrder(
                    "client_id=test_client_id",
                    "client_secret=test_client_secret",
                    "grant_type=client_credentials",
                )
        }
    }

    @Test
    fun `Fetcher should return correct error on HTTP 403, incorrect client secret`() = coroutinesExt.runTest {
        val fetcher = startMockServerCreateFetcher { createError403IncorrectClientSecretResponse() }
        val response = fetcher(TestCredentials())

        response.shouldBeInstanceOf<IgdbResult.Failure.HttpFailure<TwitchErrorResponse>>()
        response.httpCode shouldBe 403
        response.response shouldBe TwitchErrorResponse(
            status = 403,
            message = "invalid client secret",
        )
    }

    fun startMockServerCreateFetcher(
        response: (RecordedRequest) -> MockResponse? = { null },
    ): TwitchTokenFetcher {
        server.start(response)
        val url = server.url("/").toString()
        return createTwitchTokenFetcher(
            baseUrl = url,
            userAgent = "Test user agent",
            tokenTimestampSource = { 1_6868_9595_5123 },
        )
    }

    companion object {
        fun createSuccessMockResponse() = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON)
            .setBody(
                """{"access_token":"123","expires_in":5066399,"token_type":"bearer"}""",
            )

        fun createError403IncorrectClientSecretResponse() = MockResponse()
            .setResponseCode(403)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON)
            .setBody(
                """{"status":403,"message":"invalid client secret"}""",
            )

        private data class TestCredentials(
            override val clientId: String = "test_client_id",
            override val clientSecret: String = "test_client_secret",
        ) : TwitchCredentials
    }
}
