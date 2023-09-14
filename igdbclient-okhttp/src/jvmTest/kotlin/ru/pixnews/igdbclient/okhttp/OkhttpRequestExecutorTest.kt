/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.okhttp

import kotlinx.coroutines.CoroutineDispatcher
import ru.pixnews.igdbclient.integration.tests.BaseRequestExecutorTest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken

class OkhttpRequestExecutorTest : BaseRequestExecutorTest() {
    override fun createRequestExecutor(
        baseUrl: String,
        authToken: IgdbAuthToken?,
        userAgent: String?,
        headers: Map<String, List<String>>,
        backgroundDispatcher: CoroutineDispatcher,
    ): RequestExecutor {
        val okhttpClient = OkhttpExt.setupTestOkHttpClientBuilder().build()
        return OkhttpRequestExecutor(
            callFactory = okhttpClient,
            baseUrl = server.url("/v4/"),
            token = authToken,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
}
