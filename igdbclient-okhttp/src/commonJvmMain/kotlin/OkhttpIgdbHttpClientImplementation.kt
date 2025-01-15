/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.internal.IgdbHttpClient
import at.released.igdbclient.internal.RequestExecutor
import at.released.igdbclient.internal.model.IgdbAuthToken
import at.released.igdbclient.internal.model.IgdbClientConfig
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher
import at.released.igdbclient.okhttp.dsl.IgdbOkhttpConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

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
