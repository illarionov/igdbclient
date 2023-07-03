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
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

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
