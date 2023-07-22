/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor.integration

import io.ktor.client.HttpClient
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.integration.tests.BaseIgdbWebhookApiImplementationTest
import ru.pixnews.igdbclient.ktor.IgdbKtorEngine

abstract class KtorBaseIgdbWebhookApiImplementationTest : BaseIgdbWebhookApiImplementationTest() {
    override fun createIgdbClient(url: String): IgdbClient {
        return IgdbClient(IgdbKtorEngine) {
            baseUrl = url
            userAgent = "Test user agent"
            httpClient {
                httpClient = createKtorClient()
            }
        }
    }

    abstract fun createKtorClient(): HttpClient
}
