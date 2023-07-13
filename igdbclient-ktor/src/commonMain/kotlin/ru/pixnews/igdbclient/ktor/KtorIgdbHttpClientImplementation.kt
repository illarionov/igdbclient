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
