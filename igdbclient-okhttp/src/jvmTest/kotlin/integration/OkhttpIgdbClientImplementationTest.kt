/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp.integration

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.getGames
import at.released.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import at.released.igdbclient.library.test.Fixtures
import at.released.igdbclient.library.test.IgdbClientConstants
import at.released.igdbclient.library.test.okhttp.mockwebserver.MockWebServerFixtures.successMockResponseBuilder
import at.released.igdbclient.library.test.okhttp.mockwebserver.start
import at.released.igdbclient.okhttp.IgdbOkhttpEngine
import at.released.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder
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
            successMockResponseBuilder()
                .bodyDelay(50, TimeUnit.MILLISECONDS)
                .build()
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
