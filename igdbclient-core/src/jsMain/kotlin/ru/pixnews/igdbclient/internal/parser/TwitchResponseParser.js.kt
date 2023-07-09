/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.auth.model.TwitchToken
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
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
            access_token = it["access_token"] as String,
            expires_in = it.optLong("expires_in"),
            token_type = it.optString("token_type"),
        )
    }
}
