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
package ru.pixnews.igdbclient.internal

import ru.pixnews.igdbclient.auth.twitch.InMemoryTwitchTokenStorage
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig
import ru.pixnews.igdbclient.internal.twitch.TwitchAuthenticationRequestDecorator
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials

internal fun buildRequestExecutor(
    config: IgdbClientConfig<*>,
    igdbHttpClient: IgdbHttpClient,
): RequestExecutor {
    val originalExecutorFactory = igdbHttpClient.requestExecutorFactory
    val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor = if (config.retryPolicy.enabled) {
        with(config.retryPolicy) {
            { token ->
                RetryDecorator(
                    initialInterval = this.initialDelay,
                    factor = this.factor,
                    maxAttempts = this.maxRequestRetries?.let { it + 1 },
                    delayRange = this.delayRange,
                    jitterFactor = this.jitterFactor,
                    delegate = originalExecutorFactory(token),
                )
            }
        }
    } else {
        originalExecutorFactory
    }

    val twitchAuthConfig = config.twitchAuthConfig
    return if (twitchAuthConfig.enabled) {
        val credentials = object : TwitchCredentials {
            override val clientId: String = twitchAuthConfig.clientId
            override val clientSecret: String = twitchAuthConfig.clientSecret
        }
        val tokenStorage = twitchAuthConfig.storage ?: InMemoryTwitchTokenStorage()

        TwitchAuthenticationRequestDecorator(
            credentials = credentials,
            tokenStorage = tokenStorage,
            twitchTokenFetcher = igdbHttpClient.twitchTokenFetcherFactory(),
            maxRequestRetries = twitchAuthConfig.maxRequestRetries,
            requestExecutorFactory = requestExecutorFactory,
        )
    } else {
        requestExecutorFactory(null)
    }
}
