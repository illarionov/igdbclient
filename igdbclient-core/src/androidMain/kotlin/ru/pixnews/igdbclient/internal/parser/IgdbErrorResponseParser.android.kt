/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse

// XXX: copy of the jvmMain implementation

/**
 * Implementation of the Igdb server response parser using the [org.json.JSONTokener]
 */
@InternalIgdbClientApi
public actual fun IgdbParser.igdbErrorResponseParser(source: BufferedSource): IgdbHttpErrorResponse {
    val response = source.readUtf8()
    val tokener = JSONTokener(response).nextValue() as? JSONArray ?: error("Malformed JSON")
    return IgdbHttpErrorResponse(readMessageArray(tokener))
}

private fun readMessageArray(array: JSONArray): List<IgdbHttpErrorResponse.Message> {
    return (0 until array.length())
        .map { index ->
            val messageObject = array.get(index) as? JSONObject ?: error("No object at index $index")
            readMessage(messageObject)
        }
}

private fun readMessage(jsonObject: JSONObject): IgdbHttpErrorResponse.Message {
    return IgdbHttpErrorResponse.Message(
        status = jsonObject.getInt("status"),
        cause = jsonObject.getStringOrNull("cause"),
        title = jsonObject.getStringOrNull("title"),
    )
}

private fun JSONObject.getStringOrNull(key: String): String? = if (isNull(key)) null else getString(key)
