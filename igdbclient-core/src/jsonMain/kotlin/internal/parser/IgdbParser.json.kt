/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.model.IgdbWebhook
import at.released.igdbclient.model.IgdbWebhookId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okio.BufferedSource

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
