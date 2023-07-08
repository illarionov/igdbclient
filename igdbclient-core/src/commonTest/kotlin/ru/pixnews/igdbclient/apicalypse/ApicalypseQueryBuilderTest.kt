/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
}
