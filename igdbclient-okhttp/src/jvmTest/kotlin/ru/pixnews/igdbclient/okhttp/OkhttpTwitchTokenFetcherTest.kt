/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.okhttp

import okhttp3.HttpUrl.Companion.toHttpUrl
import ru.pixnews.igdbclient.integration.tests.BaseTwitchTokenFetcherTest
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder

class OkhttpTwitchTokenFetcherTest : BaseTwitchTokenFetcherTest() {
    override fun createTwitchTokenFetcher(
        baseUrl: String,
        userAgent: String?,
        tokenTimestampSource: () -> Long,
    ): TwitchTokenFetcher {
        val okhttpClient = setupTestOkHttpClientBuilder().build()
        return OkhttpTwitchTokenFetcher(
            callFactory = okhttpClient,
            baseUrl = baseUrl.toHttpUrl(),
            userAgent = userAgent,
            tokenTimestampSource = tokenTimestampSource,
        )
    }
}
