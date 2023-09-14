/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
