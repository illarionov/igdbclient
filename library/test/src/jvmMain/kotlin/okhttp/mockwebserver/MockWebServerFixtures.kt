/*
 * Copyright (c) 2024, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalOkHttpApi::class)

package ru.pixnews.igdbclient.library.test.okhttp.mockwebserver

import mockwebserver3.MockResponse
import okhttp3.ExperimentalOkHttpApi
import ru.pixnews.igdbclient.library.test.Fixtures.MockIgdbResponseContent

public object MockWebServerFixtures {
    public fun successMockResponseBuilder(): MockResponse.Builder = MockResponse.Builder()
        .code(200)
        .setHeader("Content-Type", "application/protobuf")
        .body(MockIgdbResponseContent.gamesSearch)
}
