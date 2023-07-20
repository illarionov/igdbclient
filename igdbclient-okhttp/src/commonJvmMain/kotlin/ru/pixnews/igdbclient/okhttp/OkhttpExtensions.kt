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
package ru.pixnews.igdbclient.okhttp

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal suspend fun Call.executeAsyncWithResult(): Result<Response> = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
        this.cancel()
    }
    this.enqueue(
        object : Callback {
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(
                    value = Result.failure(e),
                    onCancellation = { call.cancel() },
                )
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(
                    value = Result.success(response),
                    onCancellation = { call.cancel() },
                )
            }
        },
    )
}
