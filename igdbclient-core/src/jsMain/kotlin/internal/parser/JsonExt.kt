/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

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
