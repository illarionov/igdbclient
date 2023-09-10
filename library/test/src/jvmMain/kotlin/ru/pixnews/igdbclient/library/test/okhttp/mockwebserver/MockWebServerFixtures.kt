/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.library.test.okhttp.mockwebserver

import mockwebserver3.MockResponse
import ru.pixnews.igdbclient.library.test.Fixtures.MockIgdbResponseContent

public object MockWebServerFixtures {
    public fun createSuccessMockResponse(): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/protobuf")
        .setBody(MockIgdbResponseContent.gamesSearch)
}
