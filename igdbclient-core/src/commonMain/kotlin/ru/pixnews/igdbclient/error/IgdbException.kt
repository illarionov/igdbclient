/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.error

public open class IgdbException : RuntimeException {
    internal constructor(message: String?) : super(message)
    internal constructor(message: String?, cause: Throwable?) : super(message, cause)
    internal constructor(cause: Throwable?) : super(cause)
}
