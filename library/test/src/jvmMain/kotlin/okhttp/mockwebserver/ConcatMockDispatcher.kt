/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalOkHttpApi::class)

package at.released.igdbclient.library.test.okhttp.mockwebserver

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okhttp3.ExperimentalOkHttpApi
import okhttp3.Headers.Companion.headersOf

public open class ConcatMockDispatcher(
    private val producers: List<(RecordedRequest) -> MockResponse?>,
    private val default: MockResponse = DEFAULT_MOCK_RESPONSE,
) : Dispatcher() {
    public constructor(
        dispatcher: (RecordedRequest) -> MockResponse?,
        default: MockResponse = DEFAULT_MOCK_RESPONSE,
    ) : this(listOf(dispatcher), default)

    override fun dispatch(request: RecordedRequest): MockResponse {
        val response = producers.firstNotNullOfOrNull { it(request) }
        return response ?: default
    }

    public companion object {
        public val DEFAULT_MOCK_RESPONSE: MockResponse
            get() = MockResponse(
                code = 404,
                headers = headersOf("Content-Type", "application/json"),
                body = "Not Found",
            )
    }
}
