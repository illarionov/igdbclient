/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
