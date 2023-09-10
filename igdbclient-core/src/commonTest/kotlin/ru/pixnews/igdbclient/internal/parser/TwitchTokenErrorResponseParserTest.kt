/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import okio.Buffer
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.library.test.IgnoreAndroid
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
