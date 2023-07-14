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

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest

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
            get() = MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("Not Found")
    }
}
