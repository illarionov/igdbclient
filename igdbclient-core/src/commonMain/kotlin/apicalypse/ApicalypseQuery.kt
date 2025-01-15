/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.apicalypse

/**
 * Builds a new [ApicalypseQuery]
 */
public fun apicalypseQuery(builder: ApicalypseQueryBuilder.() -> Unit): ApicalypseQuery {
    return ApicalypseQueryBuilder().apply(builder).build()
}

/**
 * Apicalypse query.
 *
 * Use [apicalypseQuery] builder function to create and configure a new instance of a query.
 */
public interface ApicalypseQuery {
    override fun toString(): String
}
