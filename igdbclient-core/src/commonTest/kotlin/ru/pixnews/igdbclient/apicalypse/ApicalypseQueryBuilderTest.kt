/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.apicalypse

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import ru.pixnews.igdbclient.apicalypse.SortOrder.DESC
import kotlin.test.Test

class ApicalypseQueryBuilderTest {
    @Test
    fun testRequestBuilder() {
        tableOf("Builder", "Expected string result")
            .row<ApicalypseQueryBuilder.() -> Unit, String>(
                {},
                "",
            )
            .row(
                {
                    fields("*")
                },
                """f *;""",
            )
            .row(
                {
                    fields("age_ratings", "aggregated_rating", "collection.created_at", "collection.url")
                },
                "f age_ratings,aggregated_rating,collection.created_at,collection.url;",
            )
            .row(
                {
                    fields("*")
                    exclude("age_ratings", "aggregated_rating", "collection.created_at")
                    where("game.platforms = 48 & date > 1538129354")
                    limit(33)
                    offset(22)
                    sort("date", DESC)
                    search("Beyond Good & Evil")
                },
                """search "Beyond Good & Evil";""" +
                        "f *;" +
                        "x age_ratings,aggregated_rating,collection.created_at;" +
                        "w game.platforms = 48 & date > 1538129354;" +
                        "l 33;" +
                        "o 22;" +
                        "s date desc;",
            )
            .row(
                {
                    search("special symbols search \u0010 \t \u001F\u0001")
                },
                "search \"special symbols search \\u0010 \\t \\u001f\\u0001\";",
            )
            .forAll { builder, expectedResult ->
                val result = ApicalypseQueryBuilder().apply(builder).build()
                assertThat(result.toString()).isEqualTo(expectedResult)
            }
    }

    // https://github.com/illarionov/igdbclient/issues/31
    @Test
    fun whereClauseExpectedToBeUnescaped() {
        val query = apicalypseQuery {
            fields("*")
            where("""slug = "my-slug"""")
        }
        assertThat(query.toString()).isEqualTo("""f *;w slug = "my-slug";""")
    }
}
