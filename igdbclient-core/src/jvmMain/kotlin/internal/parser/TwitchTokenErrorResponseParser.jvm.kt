/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import okio.BufferedSource
import org.json.JSONObject

/**
 * Implementation of the error response parser received from Twitch server.
 *
 * Based on the [org.json.JSONTokener]
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenErrorResponseParser(source: BufferedSource): TwitchErrorResponse {
    val response = source.readUtf8()
    val jsonObject = response.jsonTokener().nextValue() as? JSONObject ?: error("Malformed JSON")
    return TwitchErrorResponse(
        status = jsonObject.optInt("status"),
        message = jsonObject.optString("message"),
    )
}
