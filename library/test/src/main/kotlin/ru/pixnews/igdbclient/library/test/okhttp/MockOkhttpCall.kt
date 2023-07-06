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
package ru.pixnews.igdbclient.library.test.okhttp

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.Timeout
import java.io.IOException

public open class MockOkhttpCall(
    private val request: Request,
    private val responseFactory: (Call) -> Result<Response>,
) : Call {
    override fun cancel(): Unit = Unit

    override fun clone(): Call = MockOkhttpCall(request, responseFactory)

    override fun enqueue(responseCallback: Callback) {
        val response = responseFactory(this)
        response.fold(
            onSuccess = {
                responseCallback.onResponse(this, it)
            },
            onFailure = {
                responseCallback.onFailure(
                    this,
                    if (it is IOException) it else IOException(it),
                )
            },
        )
    }

    override fun execute(): Response = responseFactory(this).getOrThrow()

    override fun isCanceled(): Boolean = false

    override fun isExecuted(): Boolean = true

    override fun request(): Request = request

    override fun timeout(): Timeout = Timeout.NONE

    public companion object {
        public fun factory(
            responseFactory: (Call) -> Result<Response>,
        ): Call.Factory = Call.Factory { MockOkhttpCall(it, responseFactory) }
    }
}
