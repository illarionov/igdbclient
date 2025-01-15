/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.test

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.internal.IgdbRequest
import at.released.igdbclient.internal.RequestExecutor
import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic

internal class TracingRequestExecutor(
    private val delegate: suspend (
        request: IgdbRequest,
        requestNo: Long,
    ) -> IgdbResult<Any, IgdbHttpErrorResponse>,
) : RequestExecutor {
    private val _invokeCount: AtomicLong = atomic(0L)
    val invokeCount: Long
        get() = _invokeCount.value

    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        val requestNo = _invokeCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return delegate(request, requestNo) as IgdbResult<T, IgdbHttpErrorResponse>
    }
}
