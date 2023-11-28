/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import com.squareup.wire.ofEpochSecond
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.model.dump.IgdbDump
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary

// XXX: copy of the Java implementation
@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpJsonParser(source: BufferedSource): IgdbDump {
    val response = source.readUtf8()
    val tokener = JSONTokener(response).nextValue() as? JSONObject ?: error("Malformed JSON")
    return IgdbDump(
        s3Url = tokener.getString("s3_url"),
        endpoint = tokener.getString("endpoint"),
        fileName = tokener.getString("file_name"),
        sizeBytes = tokener.getLong("size_bytes"),
        updatedAt = ofEpochSecond(tokener.getLong("updated_at"), 0),
        schemaVersion = tokener.getString("schema_version"),
        schema = tokener.optJSONObject("schema")?.let { readDumpSchema(it) },
    )
}

private fun readDumpSchema(jsonObject: JSONObject): Map<String, String> {
    val schema: MutableMap<String, String> = mutableMapOf()
    for (key in jsonObject.keys()) {
        schema[key] = jsonObject.getString(key)
    }
    return schema
}

@InternalIgdbClientApi
internal actual fun IgdbParser.igdbDumpSummaryListJsonParser(source: BufferedSource): List<IgdbDumpSummary> {
    val response = source.readUtf8()
    val tokener = JSONTokener(response).nextValue() as? JSONArray ?: error("Malformed JSON")
    return readDumpSummaryArray(tokener)
}

private fun readDumpSummaryArray(array: JSONArray): List<IgdbDumpSummary> {
    return (0 until array.length())
        .map { index ->
            val dumpObject = array.get(index) as? JSONObject ?: error("No object at index $index")
            readDumpSummary(dumpObject)
        }
}

private fun readDumpSummary(jsonObject: JSONObject): IgdbDumpSummary {
    return IgdbDumpSummary(
        endpoint = jsonObject.getString("endpoint"),
        fileName = jsonObject.getString("file_name"),
        updatedAt = ofEpochSecond(jsonObject.getLong("updated_at"), 0),
    )
}
