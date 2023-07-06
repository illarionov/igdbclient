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
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.GAME
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.PLATFORM
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.countEndpoint

class ApicalypseMultiQueryBuilderTest {
    @ParameterizedTest
    @MethodSource("testRequestBuilder")
    fun `build() should return correct request`(testData: QueryBuilderTestData) {
        val result = testData.builder.build()
        result.toString() shouldBe testData.expectedResult
    }

    companion object {
        @JvmStatic
        fun testRequestBuilder(): List<QueryBuilderTestData> = listOf(
            builderTest({}, ""),
            builderTest(
                builder = {
                    query(PLATFORM.countEndpoint(), "Count of Platforms") {
                    }
                    query(GAME, "Playstation Games") {
                        fields("name", "platforms.name")
                        where("platforms !=n & platforms = {48}")
                        limit(1)
                    }
                },
                expectedResult = """
                    |query platforms/count "Count of Platforms" {};
                    |query games "Playstation Games" {f name,platforms.name;w platforms !=n & platforms = {48};l 1;};
                """.trimMargin(),
            ),
        )

        @Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
        private fun builderTest(
            builder: ApicalypseMultiQueryBuilder.() -> Unit,
            expectedResult: String,
        ): QueryBuilderTestData {
            return QueryBuilderTestData(ApicalypseMultiQueryBuilder().apply(builder), expectedResult)
        }

        class QueryBuilderTestData(
            val builder: ApicalypseMultiQueryBuilder,
            val expectedResult: String,
        ) {
            override fun toString(): String {
                return "expectedResult='$expectedResult'"
            }
        }
    }
}
