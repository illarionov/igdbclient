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
