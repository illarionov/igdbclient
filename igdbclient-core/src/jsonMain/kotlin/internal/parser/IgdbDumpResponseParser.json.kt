/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.model.dump.IgdbDump
import at.released.igdbclient.model.dump.IgdbDumpSummary
import com.squareup.wire.ofEpochSecond
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import okio.BufferedSource

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpJsonParser(source: BufferedSource): IgdbDump {
    val jsonObject = Json.parseToJsonElement(source.readUtf8()).jsonObject
    return IgdbDump(
        s3Url = jsonObject.getValue("s3_url").jsonPrimitive.content,
        endpoint = jsonObject.getValue("endpoint").jsonPrimitive.content,
        fileName = jsonObject.getValue("file_name").jsonPrimitive.content,
        sizeBytes = jsonObject.getValue("size_bytes").jsonPrimitive.longOrNull ?: 0,
        updatedAt = ofEpochSecond(jsonObject.getValue("updated_at").jsonPrimitive.long, 0),
        schemaVersion = jsonObject.getValue("schema_version").jsonPrimitive.content,
        schema = (jsonObject["schema"] as? JsonObject)?.mapValues { it.value.asString() },
    )
}

private fun JsonElement.asString(): String = if (this is JsonPrimitive) {
    this.content
} else {
    this.toString()
}

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpSummaryListJsonParser(source: BufferedSource): List<IgdbDumpSummary> {
    val jsonElement = Json.parseToJsonElement(source.readUtf8())
    val messages = jsonElement.jsonArray
        .map { element ->
            element.jsonObject.let {
                IgdbDumpSummary(
                    endpoint = it.getValue("endpoint").jsonPrimitive.content,
                    fileName = it.getValue("file_name").jsonPrimitive.content,
                    updatedAt = ofEpochSecond(it.getValue("updated_at").jsonPrimitive.long, 0),
                )
            }
        }
    return messages
}
