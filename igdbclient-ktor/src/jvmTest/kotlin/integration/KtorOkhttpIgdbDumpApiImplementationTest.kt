/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor.integration

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.integration.tests.BaseIgdbDumpApiImplementationTest
import at.released.igdbclient.ktor.IgdbKtorEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class KtorOkhttpIgdbDumpApiImplementationTest : BaseIgdbDumpApiImplementationTest() {
    override fun createIgdbClient(url: String): IgdbClient {
        return IgdbClient(IgdbKtorEngine) {
            baseUrl = url
            userAgent = "Test user agent"
            httpClient {
                httpClient = createKtorClient()
            }
        }
    }
    fun createKtorClient(): HttpClient = HttpClient(OkHttp) {
        applyTestDefaults()
    }
}
