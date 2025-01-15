/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okio.BufferedSource

/**
 * Parser of the response received from Twitch server.
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenErrorResponseParser(source: BufferedSource): TwitchErrorResponse {
    val jsonObject = Json.parseToJsonElement(source.readUtf8()).jsonObject
    return TwitchErrorResponse(
        status = jsonObject["status"]?.jsonPrimitive?.intOrNull ?: 0,
        message = jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: "",
    )
}

/**
 * Parser for JSON responses received from the Twitch server during the Client Credentials
 * Grant Flow.
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenParser(source: BufferedSource): TwitchToken {
    val jsonObject = Json.parseToJsonElement(source.readUtf8()).jsonObject
    return TwitchToken(
        accessToken = jsonObject["access_token"]?.jsonPrimitive?.contentOrNull ?: error("no access_token"),
        expiresIn = jsonObject["expires_in"]?.jsonPrimitive?.longOrNull ?: 0,
        tokenType = jsonObject["token_type"]?.jsonPrimitive?.contentOrNull ?: "",
    )
}
