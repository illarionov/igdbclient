/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NO_BRACES_IN_CONDITIONALS_AND_LOOPS")

package at.released.igdbclient.model.dump

import com.squareup.wire.Instant

public class IgdbDumpSummary(
    public val endpoint: String,
    public val fileName: String,
    public val updatedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IgdbDumpSummary

        if (endpoint != other.endpoint) return false
        if (fileName != other.fileName) return false
        if (updatedAt.getEpochSecond() != other.updatedAt.getEpochSecond()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpoint.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    override fun toString(): String {
        return "IgdbDump(endpoint=$endpoint, fileName='$fileName', updatedAt=$updatedAt)"
    }
}
