/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NO_BRACES_IN_CONDITIONALS_AND_LOOPS")

package at.released.igdbclient.model.dump

import com.squareup.wire.Instant

@Suppress("LongParameterList")
public class IgdbDump(
    public val s3Url: String,
    public val endpoint: String,
    public val fileName: String,
    public val sizeBytes: Long,
    public val updatedAt: Instant,
    public val schemaVersion: String,
    public val schema: Map<String, String>?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IgdbDump

        if (s3Url != other.s3Url) return false
        if (endpoint != other.endpoint) return false
        if (fileName != other.fileName) return false
        if (sizeBytes != other.sizeBytes) return false
        if (updatedAt.getEpochSecond() != other.updatedAt.getEpochSecond()) return false
        if (schemaVersion != other.schemaVersion) return false
        if (schema != other.schema) return false

        return true
    }

    override fun hashCode(): Int {
        var result = s3Url.hashCode()
        result = 31 * result + endpoint.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + (schema?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "IgdbDump(s3Url='$s3Url', endpoint='$endpoint', fileName='$fileName', sizeBytes=$sizeBytes, " +
                "updatedAt=$updatedAt, schemaVersion='$schemaVersion', schema=$schema)"
    }
}
