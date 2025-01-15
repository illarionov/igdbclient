/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.library.test.okhttp.TestingLoggerInterceptor
import okhttp3.OkHttpClient
import java.time.Duration

internal object OkhttpExt {
    internal fun setupTestOkHttpClientBuilder(
        @Suppress("NewApi") timeout: Duration = Duration.ofMillis(500),
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(timeout)
        .callTimeout(timeout)
        .readTimeout(timeout)
        .writeTimeout(timeout)
        .addNetworkInterceptor(TestingLoggerInterceptor())
}
