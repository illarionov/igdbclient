/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
