/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp.integration

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.integration.tests.BaseIgdbWebhookApiImplementationTest
import at.released.igdbclient.okhttp.IgdbOkhttpEngine
import at.released.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder

internal class OkhttpIgdbWebhookApiImplementationTest : BaseIgdbWebhookApiImplementationTest() {
    override fun createIgdbClient(url: String): IgdbClient = IgdbClient(IgdbOkhttpEngine) {
        baseUrl = url
        userAgent = "Test user agent"
        httpClient {
            callFactory = setupTestOkHttpClientBuilder().build()
        }
    }
}
