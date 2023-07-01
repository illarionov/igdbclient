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
package ru.pixnews.igdbclient.error

import ru.pixnews.igdbclient.IgdbResult

public class IgdbHttpException(
    public val code: Int,
    public val httpMessage: String,
    public override val message: String,
    public val response: IgdbHttpErrorResponse?,
    public val rawResponseBody: ByteArray?,
) : IgdbException(message) {
    internal constructor(failure: IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse>) : this(
        code = failure.httpCode,
        httpMessage = failure.httpMessage,
        message = "HTTP ${failure.httpCode} ${failure.httpMessage}",
        response = failure.response,
        rawResponseBody = failure.rawResponseBody,
    )
    internal constructor(failure: IgdbResult.Failure.UnknownHttpCodeFailure) : this(
        code = failure.httpCode,
        httpMessage = failure.httpMessage,
        message = "HTTP ${failure.httpCode} ${failure.httpMessage}",
        response = null,
        rawResponseBody = failure.rawResponseBody,
    )
}
