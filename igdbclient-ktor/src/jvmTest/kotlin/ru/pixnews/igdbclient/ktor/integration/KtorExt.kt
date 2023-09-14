/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.ktor.integration

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import ru.pixnews.igdbclient.library.test.TestingLoggers
import java.time.Duration
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal fun HttpClientConfig<*>.applyTestDefaults(
    @Suppress("NewApi") timeout: Duration = Duration.ofMillis(2000),
) {
    developmentMode = true
    install(HttpTimeout) {
        connectTimeoutMillis = timeout.toMillis()
        socketTimeoutMillis = timeout.toMillis()
        requestTimeoutMillis = timeout.toMillis()
    }
    install(Logging) {
        logger = IgdbKtorLogger()
        level = LogLevel.ALL
    }
}

internal class IgdbKtorLogger(
    private val logger: Logger = TestingLoggers.consoleLogger.withTag("ktor"),
) : KtorLogger {
    override fun log(message: String) {
        logger.i(message)
    }
}
