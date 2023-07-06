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
package ru.pixnews.igdbclient.okhttp.integration

import okhttp3.OkHttpClient
import ru.pixnews.igdbclient.library.test.okhttp.TestingLoggerInterceptor
import java.time.Duration

internal object MockWebServerExt {
    fun setupTestOkHttpClientBuilder(
        @Suppress("NewApi") timeout: Duration = Duration.ofMillis(500),
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(timeout)
        .callTimeout(timeout)
        .readTimeout(timeout)
        .writeTimeout(timeout)
        .addNetworkInterceptor(TestingLoggerInterceptor())
}
