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
import okhttp3.OkHttpClient
import ru.pixnews.igdbclient.internal.IgdbHttpClient
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher
import ru.pixnews.igdbclient.okhttp.dsl.IgdbOkhttpConfig

internal class OkhttpIgdbHttpClientImplementation(
    config: IgdbClientConfig<IgdbOkhttpConfig>,
) : IgdbHttpClient {
    private val okhttpConfig = IgdbOkhttpConfig().apply(config.httpClientConfig)
    private val callFactory = okhttpConfig.callFactory ?: OkHttpClient()
    private val backgroundDispatcher = okhttpConfig.backgroundDispatcher
    private val userAgent: String? = config.userAgent
    private val baseUrl = config.baseUrl.toHttpUrl()
    private val headers = config.headers
    override val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor = { token ->
        OkhttpRequestExecutor(
            callFactory = callFactory,
            baseUrl = baseUrl,
            token = token,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
    override val twitchTokenFetcherFactory: () -> TwitchTokenFetcher = {
        OkhttpTwitchTokenFetcher(
            callFactory = callFactory,
            backgroundDispatcher = backgroundDispatcher,
            userAgent = userAgent,
        )
    }
}
