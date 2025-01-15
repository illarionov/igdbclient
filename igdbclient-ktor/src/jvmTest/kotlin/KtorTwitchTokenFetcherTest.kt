/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor

import at.released.igdbclient.integration.tests.BaseTwitchTokenFetcherTest
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher
import at.released.igdbclient.ktor.integration.applyTestDefaults
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.http.URLBuilder

class KtorTwitchTokenFetcherTest : BaseTwitchTokenFetcherTest() {
    override fun createTwitchTokenFetcher(
        baseUrl: String,
        userAgent: String?,
        tokenTimestampSource: () -> Long,
    ): TwitchTokenFetcher {
        val ktorClient = createKtorClient()
        return KtorTwitchTokenFetcher(
            httpClient = ktorClient,
            baseUrl = URLBuilder(baseUrl).build(),
            backgroundDispatcher = coroutinesExt.dispatcher,
            userAgent = userAgent,
            tokenTimestampSource = tokenTimestampSource,
        )
    }

    private fun createKtorClient(): HttpClient = HttpClient(Java) {
        applyTestDefaults()
    }
}
