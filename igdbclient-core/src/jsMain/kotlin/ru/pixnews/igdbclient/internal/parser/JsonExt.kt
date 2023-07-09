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

import kotlin.js.Json

internal fun Json.optString(key: String): String {
    return when (val value = this[key]) {
        null -> ""
        is String -> value
        else -> value.toString()
    }
}

internal fun Json.optStringOrNull(key: String): String? = this[key]?.toString()

internal fun Json.optInt(key: String): Int {
    return when (val value = this[key]) {
        null -> 0
        is Int -> value
        is Long -> value.toInt()
        else -> value.toString().toIntOrNull() ?: 0
    }
}

internal fun Json.optLong(key: String): Long {
    return when (val value = this[key]) {
        null -> 0L
        is Int -> value.toLong()
        is Long -> value
        else -> value.toString().toLongOrNull() ?: 0
    }
}

@Suppress("FUNCTION_BOOLEAN_PREFIX")
internal fun Json.optBoolean(key: String): Boolean {
    return when (val value = this[key]) {
        null -> false
        is Boolean -> value
        else -> value.toString().toBoolean()
    }
}
