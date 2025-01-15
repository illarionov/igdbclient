/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import okio.BufferedSource
import kotlin.js.Json

/**
 * Parser of the response received from Twitch server.
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenErrorResponseParser(source: BufferedSource): TwitchErrorResponse {
    val response: Any = JSON.parse(source.readUtf8())
    check(response !is Array<*>) { "Not an object" }

    return response.unsafeCast<Json>().let {
        TwitchErrorResponse(
            status = it.optInt("status"),
            message = it.optString("message"),
        )
    }
}

/**
 * Parser for JSON response with token received from the Twitch server during the Client Credentials
 * Grant Flow.
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenParser(source: BufferedSource): TwitchToken {
    val response: Any = JSON.parse(source.readUtf8())
    check(response !is Array<*>) { "Not an object" }

    return response.unsafeCast<Json>().let {
        TwitchToken(
            accessToken = it["access_token"] as String,
            expiresIn = it.optLong("expires_in"),
            tokenType = it.optString("token_type"),
        )
    }
}
