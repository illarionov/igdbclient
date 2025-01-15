/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.scheme.field

/**
 * Single field of the IGDB Entity [O].
 *
 * See [Fields](https://api-docs.igdb.com/#fields)
 */
public interface IgdbField<out O : Any> {
    /**
     * Name of the field in the IGDB API
     */
    public val igdbName: String

    public companion object {
        /**
         * Field with the value "*" used to request all fields of an entity
         */
        public val ALL: IgdbField<Nothing> = IgdbFieldAll

        private object IgdbFieldAll : IgdbField<Nothing> {
            override val igdbName: String = "*"
            override fun toString(): String = igdbName
        }
    }
}
