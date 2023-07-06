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
package ru.pixnews.igdbclient.library.test.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.logging.Level
import java.util.logging.Logger

public class TestingLoggerInterceptor(
    private val logger: Logger = Logger.getLogger("okhttp"),
    logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
) : Interceptor {
    private val loggingInterceptor = HttpLoggingInterceptor(
        logger = {
            logger.log(Level.FINE, it)
        },
    ).apply {
        level = logLevel
    }

    override fun intercept(chain: Interceptor.Chain): Response = loggingInterceptor.intercept(chain)
}
