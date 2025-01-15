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
import okio.BufferedSource
import kotlin.js.Json

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpJsonParser(source: BufferedSource): IgdbDump {
    val response: Any = JSON.parse(source.readUtf8())
    check(response !is Array<*>) { "Not an object" }

    return response.unsafeCast<Json>().let { json ->
        IgdbDump(
            s3Url = json["s3_url"] as String,
            endpoint = json["endpoint"] as String,
            fileName = json["file_name"] as String,
            sizeBytes = json.optLong("size_bytes"),
            updatedAt = ofEpochSecond(json.optLong("updated_at"), 0),
            schemaVersion = json.optString("schema_version"),
            schema = json["schema"]?.let { readDumpSchema(it.unsafeCast<Json>()) },
        )
    }
}

private fun readDumpSchema(json: Json): Map<String, String> {
    val map: MutableMap<String, String> = mutableMapOf()
    for (key in js("Object").keys(json)) {
        map[key.toString()] = json.optString(key)
    }
    return map
}

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpSummaryListJsonParser(source: BufferedSource): List<IgdbDumpSummary> {
    val response: Any = JSON.parse(source.readUtf8())
    check(response is Array<*>) { "Not a list of dumps" }

    return response.map { messageObject ->
        (messageObject.unsafeCast<Json>()).let { jsonObject ->
            IgdbDumpSummary(
                endpoint = jsonObject["endpoint"] as String,
                fileName = jsonObject["file_name"] as String,
                updatedAt = ofEpochSecond(jsonObject.optLong("updated_at"), 0),
            )
        }
    }
}
