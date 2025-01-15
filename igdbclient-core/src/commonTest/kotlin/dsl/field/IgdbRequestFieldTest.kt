/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.dsl.field

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.igdbclient.scheme.field.AgeRatingField
import at.released.igdbclient.scheme.field.GameField
import at.released.igdbclient.scheme.field.IgdbField
import kotlin.test.Test

class IgdbRequestFieldTest {
    @Test
    fun igdbFullName_should_return_correct_path_on_field_with_no_parent() {
        val field = IgdbRequestField(IgdbField.ALL, null)

        assertThat(field.igdbFullName).isEqualTo("*")
    }

    @Test
    fun igdbFullName_should_return_correct_path_on_field_with_parents() {
        val gameField = IgdbRequestField(GameField.ID)
        val ageRatingField = IgdbRequestField(
            GameField.AGE_RATINGS,
            gameField,
        )
        val contentDescription = IgdbRequestField(
            AgeRatingField.CONTENT_DESCRIPTIONS,
            ageRatingField,
        )

        assertThat(contentDescription.igdbFullName).isEqualTo("id.age_ratings.content_descriptions")
    }
}
