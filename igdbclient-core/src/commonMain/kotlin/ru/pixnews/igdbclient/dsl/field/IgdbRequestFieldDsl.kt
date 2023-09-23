/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.dsl.field

import ru.pixnews.igdbclient.dsl.IgdbClientDsl
import ru.pixnews.igdbclient.scheme.field.IgdbField

@IgdbClientDsl
public sealed class IgdbRequestFieldDsl<F : IgdbField<T>, out T : Any>(
    protected val parentIgdbField: IgdbRequestField<*>? = null,
) {
    public val all: IgdbRequestField<F> get() = IgdbRequestField(IgdbField.ALL, parentIgdbField)

    public fun fieldWithId(fieldId: F): IgdbRequestField<T> = IgdbRequestField(fieldId, parentIgdbField)
}
