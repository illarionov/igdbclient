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
package ru.pixnews.igdbclient.model

public class UnpackedMultiQueryResult<out R : Any>(
    public val name: String = "",
    public val count: Long = 0L,
    public val results: List<R>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as UnpackedMultiQueryResult<*>

        if (name != other.name) {
            return false
        }
        if (count != other.count) {
            return false
        }
        if (results != other.results) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + count.hashCode()
        result = 31 * result + (results?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "UnpackedMultiQueryResult(name='$name', count=$count, results=$results)"
    }
}
