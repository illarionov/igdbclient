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
package ru.pixnews.igdbclient.dsl

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
