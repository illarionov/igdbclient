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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.igdbclient.apicalypse.SortOrder.DESC

class ApicalypseQueryBuilderTest {
    internal class HappyPathTests {
        @ParameterizedTest
        @MethodSource("testRequestBuilder")
        fun `build should return correct request`(testData: QueryBuilderTestData) {
            val result = testData.builder.build()
            result.toString() shouldBe testData.expectedResult
        }

        companion object {
            @JvmStatic
            fun testRequestBuilder(): List<QueryBuilderTestData> = listOf(
                builderTest({}, ""),
                builderTest(
                    builder = {
                        fields("*")
                    },
                    expectedResult = """f *;""",
                ),
                builderTest(
                    builder = {
                        fields("age_ratings", "aggregated_rating", "collection.created_at", "collection.url")
                    },
                    expectedResult = "f age_ratings,aggregated_rating,collection.created_at,collection.url;",
                ),
                builderTest(
                    builder = {
                        fields("*")
                        exclude("age_ratings", "aggregated_rating", "collection.created_at")
                        where("game.platforms = 48 & date > 1538129354")
                        limit(33)
                        offset(22)
                        sort("date", DESC)
                        search("Beyond Good & Evil")
                    },
                    expectedResult = """search "Beyond Good & Evil";""" +
                            "f *;" +
                            "x age_ratings,aggregated_rating,collection.created_at;" +
                            "w game.platforms = 48 & date > 1538129354;" +
                            "l 33;" +
                            "o 22;" +
                            "s date desc;",
                ),
                builderTest(
                    builder = {
                        search("special symbols search \u0010 \t \u001F\u0001")
                    },
                    expectedResult = "search \"special symbols search \\u0010 \\t \\u001f\\u0001\";"
                )
            )

            @Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
            private fun builderTest(
                builder: ApicalypseQueryBuilder.() -> Unit,
                expectedResult: String,
            ): QueryBuilderTestData {
                return QueryBuilderTestData(ApicalypseQueryBuilder().apply(builder), expectedResult)
            }

            data class QueryBuilderTestData(
                val builder: ApicalypseQueryBuilder,
                val expectedResult: String,
            )
        }
    }
}
