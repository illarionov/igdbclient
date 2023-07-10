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
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

/**
 * Igdb server response parser
 *
 * Parse incoming [source] response from the Igdb server into a [IgdbHttpErrorResponse] object.
 *
 * Note: It is the caller's responsibility to close this stream.
 *
 * @return [IgdbHttpErrorResponse] or null if the stream cannot be parsed
 */
@InternalIgdbClientApi
public actual fun IgdbParser.igdbErrorResponseParser(source: BufferedSource): IgdbHttpErrorResponse {
    val jsonElement = Json.parseToJsonElement(source.readUtf8())
    val messages = jsonElement.jsonArray
        .map { element ->
            element.jsonObject.let {
                IgdbHttpErrorResponse.Message(
                    status = it["status"]?.jsonPrimitive?.intOrNull ?: error("Unknown status"),
                    cause = it["cause"]?.jsonPrimitive?.contentOrNull,
                    title = it["title"]?.jsonPrimitive?.contentOrNull,
                )
            }
        }
    return IgdbHttpErrorResponse(messages)
}

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbWebhookListJsonParser(source: BufferedSource): List<IgdbWebhook> {
    val jsonElement = Json.parseToJsonElement(source.readUtf8())
    return jsonElement.jsonArray.map { arrayElement ->
        arrayElement.jsonObject.let { jsonObject ->
            IgdbWebhook(
                id = IgdbWebhookId(
                    value = jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: error("No ID field"),
                ),
                url = jsonObject["url"]?.jsonPrimitive?.contentOrNull ?: "",
                category = jsonObject["category"]?.jsonPrimitive?.contentOrNull ?: "",
                subCategory = jsonObject["sub_category"]?.jsonPrimitive?.contentOrNull ?: "",
                active = jsonObject["active"]?.jsonPrimitive?.booleanOrNull ?: false,
                numberOfRetries = jsonObject["number_of_retries"]?.jsonPrimitive?.longOrNull ?: 0L,
                apiKey = jsonObject["api_key"]?.jsonPrimitive?.contentOrNull ?: "",
                secret = jsonObject["secret"]?.jsonPrimitive?.contentOrNull ?: "",
                createdAt = jsonObject["created_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                updatedAt = jsonObject["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
            )
        }
    }
}
