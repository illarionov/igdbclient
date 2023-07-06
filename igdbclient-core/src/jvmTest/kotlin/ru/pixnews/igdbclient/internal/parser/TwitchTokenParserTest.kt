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
import ru.pixnews.igdbclient.auth.model.TwitchToken

class TwitchTokenParserTest {
    private val parser = IgdbParser::twitchTokenParser

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("parserSource")
    fun `Parser should return correct result on correct data`(testSpec: ParserTestData) {
        val errorResponse = parser(testSpec.sourceBuffer)
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
        val source: String,
        val expected: TwitchToken,
    ) {
        val sourceBuffer: BufferedSource = Buffer().write(source.encodeToByteArray())

        override fun toString(): String {
            return "json: `$source` => `$expected`"
        }
    }

    internal companion object {
        @JvmStatic
        fun parserSourceMalformedData(): List<String> = listOf(
            "",
            "[]",
            "{}",
            """"{expires_in":5035365,"token_type":"bearer}"""",
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun parserSource(): List<ParserTestData> = listOf(
            ParserTestData(
                """{"access_token":"123","expires_in":5035365,"token_type":"bearer"}""",
                TwitchToken(
                    access_token = "123",
                    expires_in = 5035365,
                    token_type = "bearer",
                ),
            ),
            ParserTestData(
                """{"access_token":"123"}""",
                TwitchToken(
                    access_token = "123",
                ),
            ),
        )
    }
}
