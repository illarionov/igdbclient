/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test.okhttp

import at.released.igdbclient.library.test.TestingLoggers
import co.touchlab.kermit.Logger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

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
