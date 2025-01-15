/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor

import at.released.igdbclient.integration.tests.BaseRequestExecutorTest
import at.released.igdbclient.internal.RequestExecutor
import at.released.igdbclient.internal.model.IgdbAuthToken
import at.released.igdbclient.ktor.integration.applyTestDefaults
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CoroutineDispatcher

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
