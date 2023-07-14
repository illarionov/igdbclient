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
package ru.pixnews.igdbclient.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CoroutineDispatcher
import ru.pixnews.igdbclient.integration.tests.BaseRequestExecutorTest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.ktor.integration.applyTestDefaults

class KtorRequestExecutorTest : BaseRequestExecutorTest() {
    override fun createRequestExecutor(
        baseUrl: String,
        authToken: IgdbAuthToken?,
        userAgent: String?,
        headers: Map<String, List<String>>,
        backgroundDispatcher: CoroutineDispatcher,
    ): RequestExecutor {
        val ktorClient = HttpClient(Java) {
            applyTestDefaults()
        }
        return KtorRequestExecutor(
            httpClient = ktorClient,
            baseUrl = URLBuilder(baseUrl),
            token = authToken,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
}
