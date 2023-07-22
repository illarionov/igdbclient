/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor

import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import ru.pixnews.igdbclient.internal.IgdbHttpClient
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher

internal class KtorIgdbHttpClientImplementation(
    config: IgdbClientConfig<IgdbKtorConfig>,
) : IgdbHttpClient {
    private val ktorConfig = IgdbKtorConfig().apply(config.httpClientConfig)
    private val httpClient = ktorConfig.httpClient?.config {
        this.expectSuccess = false
    } ?: HttpClient()
    private val backgroundDispatcher = ktorConfig.backgroundDispatcher
    private val userAgent: String? = config.userAgent
    private val baseUrl = URLBuilder(config.baseUrl)
    private val headers = config.headers
    override val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor = { token ->
        KtorRequestExecutor(
            httpClient = httpClient,
            baseUrl = baseUrl,
            token = token,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
    override val twitchTokenFetcherFactory: () -> TwitchTokenFetcher = {
        KtorTwitchTokenFetcher(
            httpClient = httpClient,
            backgroundDispatcher = backgroundDispatcher,
            userAgent = userAgent,
        )
    }
}
