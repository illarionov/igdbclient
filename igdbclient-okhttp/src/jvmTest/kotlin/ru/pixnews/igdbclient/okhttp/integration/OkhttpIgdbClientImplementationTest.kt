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

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Test
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.game
import ru.pixnews.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants
import ru.pixnews.igdbclient.library.test.okhttp.start
import ru.pixnews.igdbclient.okhttp.IgdbOkhttpEngine
import ru.pixnews.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkhttpIgdbClientImplementationTest : BaseIgdbClientImplementationTest() {
    override fun createIgdbClient(baseUrl: String, authToken: String?): IgdbClient = createIgdbClient(
        okhttpClient = setupTestOkHttpClientBuilder().build(),
        baseUrl = baseUrl,
        authToken = authToken,
    )

    @Test
    fun `Implementation should not throw exceptions on cancelling while receiving response`() = coroutinesExt.runTest {
        val receivedResponseHeadersLatch = Job()
        val okhttpRequestCancelled = AtomicBoolean(false)
        val api = startServerPrepareApi(
            okhttpClient = setupTestOkHttpClientBuilder()
                .eventListener(
                    object : EventListener() {
                        override fun responseHeadersEnd(call: Call, response: Response) {
                            receivedResponseHeadersLatch.complete()
                            logger.i { "Received response headers" }
                        }

                        override fun canceled(call: Call) {
                            okhttpRequestCancelled.set(true)
                            logger.i { "Okhttp request cancelled" }
                        }
                    },
                )
                .build(),
        ) {
            Fixtures.MockIgdbResponseContent.createSuccessMockResponse()
                .setBodyDelay(50, TimeUnit.MILLISECONDS)
        }

        val request = backgroundScope.launch {
            api.game(createTestSuccessQuery())
        }
        receivedResponseHeadersLatch.join()

        request.cancelAndJoin()

        okhttpRequestCancelled.get() shouldBe true
    }

    private fun startServerPrepareApi(
        okhttpClient: OkHttpClient = setupTestOkHttpClientBuilder().build(),
        authToken: String? = Fixtures.TEST_TOKEN,
        response: (RecordedRequest) -> MockResponse? = { null },
    ): IgdbClient {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createIgdbClient(okhttpClient, url, authToken)
    }

    private fun createIgdbClient(
        okhttpClient: OkHttpClient,
        baseUrl: String,
        authToken: String? = Fixtures.TEST_TOKEN,
    ): IgdbClient {
        return IgdbClient(IgdbOkhttpEngine) {
            this.baseUrl = baseUrl
            userAgent = "Test user agent"

            httpClient {
                callFactory = okhttpClient
            }
            headers {
                append(IgdbClientConstants.Header.CLIENT_ID, Fixtures.TEST_CLIENT_ID)
                authToken?.let {
                    append(IgdbClientConstants.Header.AUTHORIZATION, "Bearer $it")
                }
                set("Header1", "HeaderValue1")
                append("Header2", "HeaderValue2")
                append("HeAdEr2", "HeaderValue22")
            }
        }
    }
}
