/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
