/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.dsl.field

import at.released.igdbclient.dsl.IgdbClientDsl
import at.released.igdbclient.scheme.field.IgdbField

@IgdbClientDsl
public sealed class IgdbRequestFieldDsl<F : IgdbField<T>, out T : Any>(
    protected val parentIgdbField: IgdbRequestField<*>? = null,
) {
    public val all: IgdbRequestField<F> get() = IgdbRequestField(IgdbField.ALL, parentIgdbField)

    public fun fieldWithId(fieldId: F): IgdbRequestField<T> = IgdbRequestField(fieldId, parentIgdbField)
}
