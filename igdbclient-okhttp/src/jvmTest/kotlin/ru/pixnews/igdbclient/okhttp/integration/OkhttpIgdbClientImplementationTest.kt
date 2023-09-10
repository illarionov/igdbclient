/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.okhttp.integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.jupiter.api.Test
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.MockWebServerFixtures.createSuccessMockResponse
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
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
            createSuccessMockResponse()
                .setBodyDelay(50, TimeUnit.MILLISECONDS)
        }

        val request = backgroundScope.launch {
            api.getGames(createTestSuccessQuery())
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
