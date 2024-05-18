/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

// XXX: copy of the jvmMain implementation

internal actual fun IgdbParser.igdbWebhookListJsonParser(source: BufferedSource): List<IgdbWebhook> {
    val response = source.readUtf8()
    val jsonArray = JSONTokener(response).nextValue() as? JSONArray ?: error("Malformed JSON")
    return (0 until jsonArray.length())
        .map { index ->
            val webhookObject = jsonArray.get(index) as? JSONObject ?: error("No object at index $index")
            parseIgdbWebhook(webhookObject)
        }
}

private fun parseIgdbWebhook(jsonObject: JSONObject): IgdbWebhook {
    return IgdbWebhook(
        id = IgdbWebhookId(
            value = jsonObject.optString("id"),
        ),
        url = jsonObject.optString("url"),
        category = jsonObject.optString("category"),
        subCategory = jsonObject.optString("sub_category"),
        active = jsonObject.optBoolean("active", false),
        numberOfRetries = jsonObject.optLong("number_of_retries"),
        apiKey = jsonObject.optString("api_key"),
        secret = jsonObject.optString("secret"),
        createdAt = jsonObject.optLong("created_at"),
        updatedAt = jsonObject.optLong("updated_at"),
    )
}
