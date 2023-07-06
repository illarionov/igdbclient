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
package ru.pixnews.igdbclient.internal.parser

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.equals.shouldBeEqual
import okio.Buffer
import okio.BufferedSource
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse.Message

class IgdbClientErrorResponseParserTest {
    private val parser = IgdbParser::igdbErrorResponseParser

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("parserSource")
    fun `Parser should return correct result on correct data`(testSpec: ParserTestData) {
        val errorResponse = parser(testSpec.sourceInputStream)
        errorResponse shouldBeEqual testSpec.expected
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("parserSourceMalformedData")
    fun `Parser should throw on malformed data`(malformedJson: String) {
        shouldThrowAny {
            parser(Buffer().write(malformedJson.encodeToByteArray()))
        }
    }

    class ParserTestData(
        val source: ByteArray,
        val expected: IgdbHttpErrorResponse,
    ) {
        val sourceInputStream: BufferedSource
            get() = Buffer().write(source)

        constructor(
            source: String,
            messages: List<Message>,
        ) : this(source.encodeToByteArray(), IgdbHttpErrorResponse(messages))

        override fun toString(): String {
            return "json: `${String(source)}` => `$expected`"
        }
    }

    internal companion object {
        @JvmStatic
        fun parserSourceMalformedData(): List<String> = listOf(
            "",
            "{}",
            "[{}]",
            """[{"status": "string"}]""",
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun parserSource(): List<ParserTestData> = listOf(
            ParserTestData("[]", emptyList()),
            ParserTestData(
                """[{"status": 100}]""",
                listOf(
                    Message(status = 100, title = null, cause = null),
                ),
            ),
            ParserTestData(
                """[{"status": "400"}]""",
                listOf(
                    Message(status = 400, title = null, cause = null),
                ),
            ),
            ParserTestData(
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
            ),
            ParserTestData(
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
            ),
        )
    }
}
