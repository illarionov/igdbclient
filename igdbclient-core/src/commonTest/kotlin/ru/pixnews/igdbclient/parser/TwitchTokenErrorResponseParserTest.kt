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
package ru.pixnews.igdbclient.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import okio.Buffer
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenErrorResponseParser
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.test.IgnoreAndroid
import kotlin.test.Test
import kotlin.test.assertFails

@IgnoreAndroid
internal class TwitchTokenErrorResponseParserTest {
    private val parser = IgdbParser::twitchTokenErrorResponseParser

    @Test
    fun parser_should_throw_on_malformed_data() = tableOf("MalformedJson")
        .row("")
        .row("[]")
        .forAll { malformedJson ->
            assertFails {
                parser(Buffer().write(malformedJson.encodeToByteArray()))
            }
        }

    @Test
    fun parser_should_return_correct_result_on_correct_data() = tableOf("Source", "Expected Result")
        .row(
            """{}""",
            TwitchErrorResponse(
                status = 0,
                message = "",
            ),
        )
        .row(
            """{"status": "string"}""",
            TwitchErrorResponse(
                status = 0,
                message = "",
            ),
        )
        .row(
            """{"status":400,"message":"invalid grant type"}""",
            TwitchErrorResponse(
                status = 400,
                message = "invalid grant type",
            ),
        )
        .forAll { testSource, expectedResult ->
            val source = Buffer().write(testSource.encodeToByteArray())
            val response = parser(source)
            assertThat(response)
                .isEqualTo(expectedResult)
        }
}
