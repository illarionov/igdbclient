/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.library.test.okhttp

import co.touchlab.kermit.Logger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.pixnews.igdbclient.library.test.TestingLoggers

public class TestingLoggerInterceptor(
    private val logger: Logger = TestingLoggers.consoleLogger.withTag("okhttp"),
    logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
) : Interceptor {
    private val loggingInterceptor = HttpLoggingInterceptor(
        logger = { logger.i(it) },
    ).apply {
        level = logLevel
    }

    override fun intercept(chain: Interceptor.Chain): Response = loggingInterceptor.intercept(chain)
}
