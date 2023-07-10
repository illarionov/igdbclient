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
package ru.pixnews.igdbclient.ktor.integration

import io.ktor.client.HttpClient
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import ru.pixnews.igdbclient.ktor.IgdbKtorEngine
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants

abstract class KtorBaseIgdbClientImplementationTest : BaseIgdbClientImplementationTest() {
    abstract fun createKtorClient(): HttpClient

    override fun createIgdbClient(baseUrl: String, authToken: String?): IgdbClient {
        return IgdbClient(IgdbKtorEngine) {
            this.baseUrl = baseUrl
            userAgent = "Test user agent"

            httpClient {
                httpClient = createKtorClient()
            }
            headers {
                append(IgdbClientConstants.Header.CLIENT_ID, Fixtures.TEST_CLIENT_ID)
                authToken?.let {
                    append(IgdbClientConstants.Header.AUTHORIZATION, "Bearer $it")
                }
                set("Header1", "HeaderValue1")
                append("Header2", "HeaderValue2")
                append("HeAdEr2", "HeaderValue22")
            }
        }
    }
}
