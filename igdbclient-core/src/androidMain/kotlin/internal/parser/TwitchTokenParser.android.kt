/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.TwitchToken
import okio.BufferedSource
import org.json.JSONObject
import org.json.JSONTokener

// XXX: copy of the jvmMain implementation

/**
 * Implementation of a parser for JSON responses received from the Twitch server during the Client Credentials
 * Grant Flow.
 * Based on the [org.json.JSONTokener]
 */
@InternalIgdbClientApi
public actual fun IgdbParser.twitchTokenParser(source: BufferedSource): TwitchToken {
    val response = source.readUtf8()
    val jsonObject = JSONTokener(response).nextValue() as? JSONObject ?: error("Malformed JSON")
    return TwitchToken(
        accessToken = jsonObject.getString("access_token"),
        expiresIn = jsonObject.optLong("expires_in"),
        tokenType = jsonObject.optString("token_type"),
    )
}
