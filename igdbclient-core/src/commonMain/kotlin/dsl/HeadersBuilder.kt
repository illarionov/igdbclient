/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.dsl

@Suppress("FUNCTION_BOOLEAN_PREFIX", "TooManyFunctions", "MISSING_KDOC_ON_FUNCTION")
@IgdbClientDsl
public class HeadersBuilder {
    private val values: MutableMap<String, MutableList<String>> = mutableMapOf()

    public fun getAll(name: String): List<String>? = values[name]

    public operator fun contains(name: String): Boolean = name in values

    public fun contains(name: String, value: String): Boolean = values[name]?.contains(value) ?: false

    public fun entries(): Set<Map.Entry<String, List<String>>> = values.entries

    public operator fun set(name: String, value: String) {
        values[name] = mutableListOf(value)
    }

    public operator fun get(name: String): String? = values[name]?.firstOrNull()

    public fun append(name: String, value: String) {
        values.getOrPut(name, ::mutableListOf).add(value)
    }

    public fun remove(name: String) {
        values.remove(name)
    }

    public fun remove(name: String, value: String): Boolean = values[name]?.remove(value) ?: false

    public fun clear() {
        values.clear()
    }

    internal fun build(): Map<String, List<String>> {
        return values.mapValues {
            it.value.toList()
        }
    }
}
