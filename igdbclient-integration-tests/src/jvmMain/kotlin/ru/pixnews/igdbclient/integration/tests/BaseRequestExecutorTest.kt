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
package ru.pixnews.igdbclient.integration.tests

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.Header.AUTHORIZATION
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.Header.CLIENT_ID
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start

@Suppress("FunctionName")
abstract class BaseRequestExecutorTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    abstract fun createRequestExecutor(
        baseUrl: String,
        authToken: IgdbAuthToken? = null,
        userAgent: String? = "Test user agent",
        headers: Map<String, List<String>> = emptyMap(),
        backgroundDispatcher: CoroutineDispatcher = coroutinesExt.dispatcher,
    ): RequestExecutor

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `User headers should take precedence over AuthToken-provided headers `() = coroutinesExt.runTest {
        val testAuthToken = object : IgdbAuthToken {
            override val clientId: String = "authtoken-provided-client-id"
            override val token: String = "authtoken-provided-token"
        }

        val executor = startMockServerCreateClient(
            token = testAuthToken,
            headers = mapOf(
                CLIENT_ID to listOf(Fixtures.TEST_CLIENT_ID),
                AUTHORIZATION to listOf("Bearer ${Fixtures.TEST_TOKEN}"),
            ),
        )

        executor.invoke<Any>(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _, _ -> "" }),
        )

        server.takeRequest().run {
            headers.values("Client-Id") shouldBe listOf(Fixtures.TEST_CLIENT_ID)
            headers.values("Authorization") shouldBe listOf("Bearer ${Fixtures.TEST_TOKEN}")
        }
    }

    @Test
    fun `Executor should not encode slashes in path`() = coroutinesExt.runTest {
        val executor = startMockServerCreateClient()

        executor.invoke<Any>(
            ApicalypsePostRequest("games/count", apicalypseQuery { }, { _, _ -> "" }),
        )

        server.takeRequest().run {
            path shouldBe "/v4/games/count"
        }
    }

    private fun startMockServerCreateClient(
        token: IgdbAuthToken? = null,
        userAgent: String? = "Test user agent",
        headers: Map<String, List<String>> = emptyMap(),
        backgroundDispatcher: CoroutineDispatcher = coroutinesExt.dispatcher,
        response: (RecordedRequest) -> MockResponse? = { null },
    ): RequestExecutor {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createRequestExecutor(
            baseUrl = url,
            authToken = token,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
}
