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
package ru.pixnews.igdbclient.okhttp.integration

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.library.test.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.ConcatMockDispatcher
import ru.pixnews.igdbclient.okhttp.OkhttpIgdbConstants.MediaType
import ru.pixnews.igdbclient.okhttp.OkhttpTwitchTokenFetcher
import ru.pixnews.igdbclient.okhttp.integration.MockWebServerExt.setupTestOkHttpClientBuilder
import java.time.Instant

class OkhttpTwitchTokenFetcherTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    private val server: MockWebServer = MockWebServer()
    private lateinit var fetcher: OkhttpTwitchTokenFetcher

    @Test
    fun `Fetcher should correctly parse success 200 response`() = coroutinesExt.runTest {
        startServerPrepareApi { request ->
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
        startServerPrepareApi { createSuccessMockResponse() }

        fetcher(TestCredentials())

        server.takeRequest().run {
            headers.values("Accept") shouldBe listOf("application/json")
            headers.values("User-Agent") shouldBe listOf("Test User Agent")
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
        startServerPrepareApi { createError403IncorrectClientSecretResponse() }
        val response = fetcher(TestCredentials())

        response.shouldBeInstanceOf<HttpFailure<TwitchErrorResponse>>()
        response.httpCode shouldBe 403
        response.response shouldBe TwitchErrorResponse(
            status = 403,
            message = "invalid client secret",
        )
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun startServerPrepareApi(
        okhttpClient: OkHttpClient = setupTestOkHttpClientBuilder().build(),
        response: (RecordedRequest) -> MockResponse? = { null },
    ) {
        val testServerDispatcher = ConcatMockDispatcher(response)
        server.dispatcher = testServerDispatcher
        server.start()

        fetcher = OkhttpTwitchTokenFetcher(
            callFactory = okhttpClient,
            baseUrl = server.url("/"),
            userAgent = "Test User Agent",
            tokenTimestampSource = { 1_6868_9595_5123 },
        )
    }

    private companion object {
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
