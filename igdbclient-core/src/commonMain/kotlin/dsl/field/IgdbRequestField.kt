/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.dsl.field

import at.released.igdbclient.dsl.IgdbClientDsl
import at.released.igdbclient.scheme.field.IgdbField

@IgdbClientDsl
public data class IgdbRequestField<out O : Any> internal constructor(
    public val igdbField: IgdbField<O>,
    public val parent: IgdbRequestField<*>? = null,
) {
    public val igdbFullName: String
        get() = if (parent == null) {
            this.igdbField.igdbName
        } else {
            parent.igdbFullName + "." + this.igdbField.igdbName
        }

    override fun toString(): String = igdbFullName
}
