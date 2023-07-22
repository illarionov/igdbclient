/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.error

public data class IgdbHttpErrorResponse(
    val messages: List<Message>,
) {
    public data class Message(
        val status: Int,
        val title: String?,
        val cause: String?,
    )
}
