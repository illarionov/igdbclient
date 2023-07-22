/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
