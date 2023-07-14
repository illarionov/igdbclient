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
package ru.pixnews.igdbclient.library.test.okhttp.mockwebserver

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest

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
