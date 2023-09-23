/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.scheme.field

public interface IgdbField<out O : Any> {
    public val igdbName: String

    public companion object {
        public val ALL: IgdbField<Nothing> = IgdbFieldAll

        private object IgdbFieldAll : IgdbField<Nothing> {
            override val igdbName: String = "*"
            override fun toString(): String = igdbName
        }
    }
}
