/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
