/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.twitch

import ru.pixnews.igdbclient.InternalIgdbClientApi

@InternalIgdbClientApi
public data class TwitchErrorResponse(
    val status: Int,
    val message: String,
)
