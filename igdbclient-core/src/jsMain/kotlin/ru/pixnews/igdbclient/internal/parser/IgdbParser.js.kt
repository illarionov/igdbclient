/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId
import kotlin.js.Json

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
    val response: Any = JSON.parse(source.readUtf8())
    check(response is Array<*>) { "Not a list of messages" }

    val messages = response.map { messageObject ->
        (messageObject.unsafeCast<Json>()).let { jsonObject ->
            IgdbHttpErrorResponse.Message(
                status = jsonObject["status"].toString().toInt(),
                cause = jsonObject.optStringOrNull("cause"),
                title = jsonObject.optStringOrNull("title"),
            )
        }
    }
    return IgdbHttpErrorResponse(messages)
}

internal actual fun IgdbParser.igdbWebhookListJsonParser(source: BufferedSource): List<IgdbWebhook> {
    val response: Any = JSON.parse(source.readUtf8())
    check(response is Array<*>) { "Not a list of webhooks" }
    return response.map { messageObject ->
        (messageObject.unsafeCast<Json>()).let { jsonObject ->
            IgdbWebhook(
                id = IgdbWebhookId(
                    value = jsonObject.optStringOrNull("id") ?: error("No ID field"),
                ),
                url = jsonObject.optString("url"),
                category = jsonObject.optString("category"),
                subCategory = jsonObject.optString("sub_category"),
                active = jsonObject.optBoolean("active"),
                numberOfRetries = jsonObject.optLong("number_of_retries"),
                apiKey = jsonObject.optString("api_key"),
                secret = jsonObject.optString("secret"),
                createdAt = jsonObject.optLong("created_at"),
                updatedAt = jsonObject.optLong("updated_at"),
            )
        }
    }
}
