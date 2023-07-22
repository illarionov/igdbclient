/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import org.json.JSONObject
import org.json.JSONTokener
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse

/**
 * Implementation of the error response parser received from Twitch server.
 *
 * Based on the [org.json.JSONTokener]
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenErrorResponseParser(source: BufferedSource): TwitchErrorResponse {
    val response = source.readUtf8()
    val jsonObject = JSONTokener(response).nextValue() as? JSONObject ?: error("Malformed JSON")
    return TwitchErrorResponse(
        status = jsonObject.optInt("status"),
        message = jsonObject.optString("message"),
    )
}
