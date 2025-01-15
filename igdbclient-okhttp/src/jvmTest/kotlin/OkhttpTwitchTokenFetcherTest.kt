/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.integration.tests.BaseTwitchTokenFetcherTest
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher
import at.released.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl

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
