/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.http.URLBuilder
import ru.pixnews.igdbclient.integration.tests.BaseTwitchTokenFetcherTest
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.ktor.integration.applyTestDefaults

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
