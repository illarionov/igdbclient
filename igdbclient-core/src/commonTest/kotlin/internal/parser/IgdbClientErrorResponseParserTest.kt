/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import okio.Buffer
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse.Message
import ru.pixnews.igdbclient.library.test.IgnoreAndroid
import kotlin.test.Test
import kotlin.test.assertFails

@IgnoreAndroid
class IgdbClientErrorResponseParserTest {
    private val parser = IgdbParser::igdbErrorResponseParser

    @Test
    fun parser_should_throw_on_malformed_data() = tableOf("MalformedJson")
        .row("")
        .row("{}")
        .row("[{}]")
        .row("""[{"status": "string"}]""")
        .forAll { malformedJson ->
            assertFails {
                parser(Buffer().write(malformedJson.encodeToByteArray()))
            }
        }

    @Test
    fun parser_should_return_correct_result_on_correct_data() = tableOf("Source", "Expected Result")
        .row(
            "[]",
            emptyList<Message>(),
        )
        .row(
            """[{"status": 100}]""",
            listOf(
                Message(status = 100, title = null, cause = null),
            ),
        )
        .row(
            """[{"status": "400"}]""",
            listOf(
                Message(status = 400, title = null, cause = null),
            ),
        )
        .row(
            """
                [
                  {
                    "title": "Syntax Error",
                    "status": 400,
                    "cause": "Expecting a STRING as input, surround your input with quotes starting at \u0027Diablo\u0027"
                  }
                ]
            """.trimIndent(),
            listOf(
                Message(
                    status = 400,
                    title = "Syntax Error",
                    cause = "Expecting a STRING as input, surround your input with quotes starting at 'Diablo'",
                ),
            ),
        )
        .row(
            """
                [
                  {
                    "status": 500,
                    "cause": "Cause 1"
                  },
                  {
                    "title": "Title 2",
                    "status": 400
                  },
                  {
                    "status": 401,
                    "cause": "Cause 3",
                    "title": "Title 3"
                  }
                ]
            """.trimIndent(),
            listOf(
                Message(
                    status = 500,
                    title = null,
                    cause = "Cause 1",
                ),
                Message(
                    status = 400,
                    title = "Title 2",
                    cause = null,
                ),
                Message(
                    status = 401,
                    title = "Title 3",
                    cause = "Cause 3",
                ),
            ),
        )
        .forAll { testSource, expectedResult ->
            val source = Buffer().write(testSource.encodeToByteArray())
            val response = parser(source).messages
            assertThat(response)
                .isEqualTo(expectedResult)
        }
}
