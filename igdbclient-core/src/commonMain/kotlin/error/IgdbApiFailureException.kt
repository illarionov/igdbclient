/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.error

import at.released.igdbclient.IgdbResult

public open class IgdbApiFailureException(
    message: String?,
    cause: Throwable?,
) : IgdbException(message, cause) {
    internal constructor(response: IgdbResult.Failure.ApiFailure) : this(
        message = response.error?.message,
        cause = response.error,
    )
}
