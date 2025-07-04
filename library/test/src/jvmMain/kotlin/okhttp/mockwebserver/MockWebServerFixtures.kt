/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test.okhttp.mockwebserver

import at.released.igdbclient.library.test.Fixtures.MockIgdbResponseContent
import mockwebserver3.MockResponse

public object MockWebServerFixtures {
    public fun successMockResponseBuilder(): MockResponse.Builder = MockResponse.Builder()
        .code(200)
        .setHeader("Content-Type", "application/protobuf")
        .body(MockIgdbResponseContent.gamesSearch)
}
