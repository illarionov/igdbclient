/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.okhttp.integration

import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.integration.tests.BaseIgdbWebhookApiImplementationTest
import ru.pixnews.igdbclient.okhttp.IgdbOkhttpEngine
import ru.pixnews.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder

internal class OkhttpIgdbWebhookApiImplementationTest : BaseIgdbWebhookApiImplementationTest() {
    override fun createIgdbClient(url: String): IgdbClient = IgdbClient(IgdbOkhttpEngine) {
        baseUrl = url
        userAgent = "Test user agent"
        httpClient {
            callFactory = setupTestOkHttpClientBuilder().build()
        }
    }
}
