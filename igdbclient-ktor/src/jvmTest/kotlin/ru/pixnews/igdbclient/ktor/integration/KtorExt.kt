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
    @Suppress("NewApi") timeout: Duration = Duration.ofMillis(700),
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
