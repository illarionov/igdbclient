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

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbWebhookApi
import ru.pixnews.igdbclient.library.test.IgdbClientConstants
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

/**
 * Base class with common tests running on different implementations of the IgdbWebhookApi
 */
abstract class BaseIgdbWebhookApiImplementationTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    abstract fun createIgdbClient(url: String): IgdbClient

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun startMockServerCreateClient(
        response: (RecordedRequest) -> MockResponse? = { null },
    ): IgdbWebhookApi {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createIgdbClient(url).webhookApi
    }

    companion object {
        const val SINGLE_WEBHOOK_RESPONSE: String = """
[
  {
    "id": 7136,
    "url": "https://example.com/game/1/",
    "category": 625691411,
    "sub_category": 2,
    "active": true,
    "number_of_retries": 0,
    "api_key": "api_key_1",
    "secret": "my_secret",
    "created_at": 1688017255,
    "updated_at": 1688017855
  }
]
        """
        val SINGLE_WEBHOOK_RESPONSE_IGDB_WEBHOOK = IgdbWebhook(
            id = IgdbWebhookId("7136"),
            url = "https://example.com/game/1/",
            category = "625691411",
            subCategory = "2",
            active = true,
            numberOfRetries = 0,
            apiKey = "api_key_1",
            secret = "my_secret",
            createdAt = 1_688_017_255,
            updatedAt = 1_688_017_855,
        )

        fun createSuccessMockResponse(response: String = SINGLE_WEBHOOK_RESPONSE) = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
            .setBody(response)
    }

    @Nested
    @DisplayName("registerWebhook()")
    inner class RegisterWebhookTests {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/games/webhooks") createSuccessMockResponse() else null
        }

        @Test
        fun `registerWebhook() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.registerWebhook(
                endpoint = IgdbEndpoint.GAME,
                url = "https://example.com/game/1/",
                method = IgdbWebhookApi.WebhookMethod.CREATE,
                secret = "my_secret",
            ) as? IgdbResult.Success<List<IgdbWebhook>>

            checkNotNull(response).value.shouldContainOnly(SINGLE_WEBHOOK_RESPONSE_IGDB_WEBHOOK)
        }

        @Test
        fun `registerWebhook() should send correct request`() = coroutinesExt.runTest {
            api.registerWebhook(
                endpoint = IgdbEndpoint.GAME,
                url = "https://example.com/game/1/",
                method = IgdbWebhookApi.WebhookMethod.CREATE,
                secret = "my_secret",
            )

            server.takeRequestWithTimeout().run {
                method shouldBe "POST"
                body.readByteString().utf8().split('&').shouldContainExactlyInAnyOrder(
                    "url=https%3A%2F%2Fexample.com%2Fgame%2F1%2F",
                    "method=create",
                    "secret=my_secret",
                )
                headers.values("Accept") shouldBe listOf("application/json")
                headers.values("Content-Type").shouldBeIn(
                    listOf("application/x-www-form-urlencoded"),
                    listOf("application/x-www-form-urlencoded; charset=UTF-8"),
                )
            }
        }
    }

    @Nested
    @DisplayName("getAllWebhooks()")
    inner class GetWebhooksTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/webhooks") createSuccessMockResponse() else null
        }

        @Test
        fun `getAllWebhooks() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.getAllWebhooks()
                    as? IgdbResult.Success<List<IgdbWebhook>>
            checkNotNull(response).value.shouldContainOnly(SINGLE_WEBHOOK_RESPONSE_IGDB_WEBHOOK)
        }

        @Test
        fun `getAllWebhooks() should send correct request`() = coroutinesExt.runTest {
            api.getAllWebhooks()

            server.takeRequestWithTimeout().run {
                method shouldBe "GET"
                headers.values("Accept") shouldBe listOf("application/json")
            }
        }
    }

    @Nested
    @DisplayName("getWebhook()")
    inner class GetWebhookTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/webhooks/7136") createSuccessMockResponse() else null
        }

        @Test
        fun `getWebhook() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.getWebhook(
                IgdbWebhookId("7136"),
            ) as? IgdbResult.Success<List<IgdbWebhook>>
            checkNotNull(response).value.shouldContainOnly(SINGLE_WEBHOOK_RESPONSE_IGDB_WEBHOOK)
        }

        @Test
        fun `getWebhook() should send correct request`() = coroutinesExt.runTest {
            api.getWebhook(IgdbWebhookId("7136"))

            server.takeRequestWithTimeout().run {
                method shouldBe "GET"
                headers.values("Accept") shouldBe listOf("application/json")
            }
        }
    }

    @Nested
    @DisplayName("deleteWebhook()")
    inner class DeleteWebhookTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/webhooks/7136") createSuccessMockResponse() else null
        }

        @Test
        fun `deleteWebhook() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.deleteWebhook(
                IgdbWebhookId("7136"),
            ) as? IgdbResult.Success<List<IgdbWebhook>>
            checkNotNull(response).value.shouldContainOnly(SINGLE_WEBHOOK_RESPONSE_IGDB_WEBHOOK)
        }

        @Test
        fun `deleteWebhook() should send correct request`() = coroutinesExt.runTest {
            api.deleteWebhook(IgdbWebhookId("7136"))

            server.takeRequestWithTimeout().run {
                method shouldBe "DELETE"
                headers.values("Accept") shouldBe listOf("application/json")
            }
        }
    }

    @Nested
    @DisplayName("testWebhook()")
    inner class TestWebhookTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/games/webhooks/test/7138?entityId=12") {
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
                    .setBody("OK")
            } else {
                null
            }
        }

        @Test
        fun `testWebhook() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.testWebhook(
                endpoint = IgdbEndpoint.GAME,
                webhookId = IgdbWebhookId("7138"),
                entityId = "12",
            ) as? IgdbResult.Success<String>
            checkNotNull(response).value.shouldBe("OK")
        }

        @Test
        fun `testWebhook() should send correct request`() = coroutinesExt.runTest {
            api.testWebhook(
                endpoint = IgdbEndpoint.GAME,
                webhookId = IgdbWebhookId("7136"),
                entityId = "12",
            ) as? IgdbResult.Success<String>

            server.takeRequestWithTimeout().run {
                method shouldBe "POST"
                body.size shouldBe 0
                headers.values("Accept") shouldBe listOf("application/json")
            }
        }
    }
}
