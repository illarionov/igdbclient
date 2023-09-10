/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
            return@with { token ->
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
