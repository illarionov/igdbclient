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
import ru.pixnews.igdbclient.auth.model.TwitchToken
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenParser
import ru.pixnews.igdbclient.test.IgnoreAndroid
import ru.pixnews.igdbclient.test.IgnoreNative
import kotlin.test.Test
import kotlin.test.assertFails

@IgnoreAndroid
@IgnoreNative
class TwitchTokenParserTest {
    private val parser = IgdbParser::twitchTokenParser

    @Test
    fun parser_should_throw_on_malformed_data() = tableOf("MalformedJson")
        .row("")
        .row("[]")
        .row("{}")
        .row(""""{expires_in":5035365,"token_type":"bearer}"""")
        .forAll { malformedJson ->
            assertFails {
                parser(Buffer().write(malformedJson.encodeToByteArray()))
            }
        }

    @Test
    fun parser_should_return_correct_result_on_correct_data() = tableOf("Source", "Expected Result")
        .row(
            """{"access_token":"123","expires_in":5035365,"token_type":"bearer"}""",
            TwitchToken(
                access_token = "123",
                expires_in = 5035365,
                token_type = "bearer",
            ),
        )
        .row(
            """{"access_token":"123"}""",
            TwitchToken(
                access_token = "123",
            ),
        )
        .forAll { testSource, expectedResult ->
            val source = Buffer().write(testSource.encodeToByteArray())
            val response = parser(source)
            assertThat(response)
                .isEqualTo(expectedResult)
        }
}
