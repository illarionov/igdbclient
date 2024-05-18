/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.squareup.wire.ofEpochSecond
import okio.Buffer
import ru.pixnews.igdbclient.library.test.IgnoreAndroid
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary
import kotlin.test.Test
import kotlin.test.assertFails

@IgnoreAndroid
class IgdbDumpSummaryListJsonParserTest {
    private val parser = IgdbParser::igdbDumpSummaryListJsonParser

    @Test
    fun parser_should_throw_on_malformed_data() = tableOf("MalformedJson")
        .row("")
        .row("{}")
        .forAll { malformedJson ->
            assertFails {
                parser(Buffer().write(malformedJson.encodeToByteArray()))
            }
        }

    @Test
    fun parser_should_return_correct_result_on_correct_data() = tableOf("Source", "Expected Result")
        .row("""[]""", listOf<IgdbDumpSummary>())
        .row(
            """[{"endpoint": "games", "file_name": "1234567890_games.csv", "updated_at": 1234567890}]""",
            listOf(IgdbDumpSummary("games", "1234567890_games.csv", ofEpochSecond(1_234_567_890L, 0))),
        )
        .row(
            """[
              {"endpoint": "games", "file_name": "1234567890_games.csv", "updated_at": 1234567890},
              {"endpoint": "age_ratings", "file_name": "1234567890_age_ratings.csv", "updated_at": 1234567891}
            ]
            """.trimIndent(),
            listOf(
                IgdbDumpSummary("games", "1234567890_games.csv", ofEpochSecond(1_234_567_890L, 0)),
                IgdbDumpSummary("age_ratings", "1234567890_age_ratings.csv", ofEpochSecond(1_234_567_891L, 0)),
            ),
        )
        .forAll { testSource, expectedResult ->
            val source = Buffer().write(testSource.encodeToByteArray())
            val response = parser(source)
            assertThat(response)
                .isEqualTo(expectedResult)
        }
}
