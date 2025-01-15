/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalOkHttpApi::class)

package at.released.igdbclient.library.test.okhttp.mockwebserver

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.ExperimentalOkHttpApi
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public fun MockWebServer.takeRequestWithTimeout(
    timeout: Duration = 10.seconds,
): RecordedRequest = checkNotNull(takeRequest(timeout = timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)) {
    "timeout during takeRequest()"
}

public fun MockWebServer.start(
    response: (RecordedRequest) -> MockResponse? = { null },
) {
    start(0, response)
}

public fun MockWebServer.start(
    port: Int = 0,
    response: (RecordedRequest) -> MockResponse? = { null },
) {
    val testServerDispatcher = ConcatMockDispatcher(response)
    dispatcher = testServerDispatcher
    start(port)
}
