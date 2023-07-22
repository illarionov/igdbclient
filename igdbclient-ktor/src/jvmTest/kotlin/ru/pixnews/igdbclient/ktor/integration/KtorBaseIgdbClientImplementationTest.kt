/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor.integration

import io.ktor.client.HttpClient
import mockwebserver3.SocketPolicy
import org.junit.jupiter.api.Disabled
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import ru.pixnews.igdbclient.ktor.IgdbKtorEngine
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants

abstract class KtorBaseIgdbClientImplementationTest : BaseIgdbClientImplementationTest() {
    abstract fun createKtorClient(): HttpClient

    override fun createIgdbClient(baseUrl: String, authToken: String?): IgdbClient {
        return IgdbClient(IgdbKtorEngine) {
            this.baseUrl = baseUrl
            userAgent = "Test user agent"

            httpClient {
                httpClient = createKtorClient()
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

    @Suppress("BACKTICKS_PROHIBITED")
    @Disabled("Flaky and slow")
    override fun `Implementation should throw correct exception on network error`(policy: SocketPolicy) {
        super.`Implementation should throw correct exception on network error`(policy)
    }
}
