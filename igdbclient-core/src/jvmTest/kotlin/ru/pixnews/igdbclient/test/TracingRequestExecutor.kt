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
package ru.pixnews.igdbclient.test

import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import java.util.concurrent.atomic.AtomicLong

internal class TracingRequestExecutor(
    private val delegate: suspend (
        request: IgdbRequest,
        requestNo: Long,
    ) -> IgdbResult<Any, IgdbHttpErrorResponse>,
) : RequestExecutor {
    private val _invokeCount: AtomicLong = AtomicLong(0)
    val invokeCount: Long
        get() = _invokeCount.get()

    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        val requestNo = _invokeCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return delegate(request, requestNo) as IgdbResult<T, IgdbHttpErrorResponse>
    }

    companion object
}
