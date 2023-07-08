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
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.countEndpoint
import kotlin.test.Test

class ApicalypseMultiQueryBuilderTest {
    @Test
    @Suppress("TrailingCommaOnCallSite")
    fun testRequestBuilder() = tableOf("Builder", "Expected string result")
        .row<ApicalypseMultiQueryBuilder.() -> Unit, String>(
            {
                query(IgdbEndpoint.PLATFORM.countEndpoint(), "Count of Platforms") {
                }
                query(IgdbEndpoint.GAME, "Playstation Games") {
                    fields("name", "platforms.name")
                    where("platforms !=n & platforms = {48}")
                    limit(1)
                }
            },
            """
                |query platforms/count "Count of Platforms" {};
                |query games "Playstation Games" {f name,platforms.name;w platforms !=n & platforms = {48};l 1;};
            """.trimMargin()
        ).forAll { builder, expectedResult ->
            val result = ApicalypseMultiQueryBuilder().apply(builder).build()
            assertThat(result.toString()).isEqualTo(expectedResult)
        }
}
