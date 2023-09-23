/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.dsl.field

import assertk.assertThat
import assertk.assertions.isEqualTo
import ru.pixnews.igdbclient.scheme.field.AgeRatingField
import ru.pixnews.igdbclient.scheme.field.GameField
import ru.pixnews.igdbclient.scheme.field.IgdbField
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
