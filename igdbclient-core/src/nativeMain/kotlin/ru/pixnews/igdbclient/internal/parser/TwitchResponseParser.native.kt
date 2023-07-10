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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.auth.model.TwitchToken
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse

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
        access_token = jsonObject["access_token"]?.jsonPrimitive?.contentOrNull ?: error("no access_token"),
        expires_in = jsonObject["expires_in"]?.jsonPrimitive?.longOrNull ?: 0,
        token_type = jsonObject["token_type"]?.jsonPrimitive?.contentOrNull ?: "",
    )
}