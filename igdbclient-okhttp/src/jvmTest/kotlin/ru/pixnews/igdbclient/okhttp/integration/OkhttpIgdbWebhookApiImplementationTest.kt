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

import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.integration.tests.BaseIgdbWebhookApiImplementationTest
import ru.pixnews.igdbclient.okhttp.IgdbOkhttpEngine
import ru.pixnews.igdbclient.okhttp.OkhttpExt.setupTestOkHttpClientBuilder

internal class OkhttpIgdbWebhookApiImplementationTest : BaseIgdbWebhookApiImplementationTest() {
    override fun createIgdbClient(url: String): IgdbClient = IgdbClient(IgdbOkhttpEngine) {
        baseUrl = url
        userAgent = "Test user agent"
        httpClient {
            callFactory = setupTestOkHttpClientBuilder().build()
        }
    }
}
