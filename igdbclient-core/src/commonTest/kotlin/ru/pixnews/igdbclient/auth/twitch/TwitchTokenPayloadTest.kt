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
package ru.pixnews.igdbclient.auth.twitch

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.squareup.wire.ofEpochSecond
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload.Companion.getTwitchAccessToken
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.model.TwitchToken.Companion.encode
import kotlin.test.Test

class TwitchTokenPayloadTest {
    @Test
    fun invoke_with_string_token_should_return_correct_payload() {
        val payload = TwitchTokenPayload("testToken")

        val decodedToken = TwitchToken.decode(payload.payload)

        assertThat(decodedToken)
            .isEqualTo(
                TwitchToken(
                    accessToken = "testToken",
                ),
            )
    }

    @Test
    fun getTwitchToken_should_return_correct_value() {
        val payload = TwitchTokenPayload(validToken1.encode())

        val twitchToken = payload.getTwitchAccessToken()

        assertThat(twitchToken).isEqualTo("testValidToken1")
    }

    @Test
    fun getTwitchToken_should_return_null_on_no_token() {
        val payload = TwitchTokenPayload.NO_TOKEN

        val twitchToken = payload.getTwitchAccessToken()

        assertThat(twitchToken).isNull()
    }

    private companion object {
        private val validToken1: TwitchToken = TwitchToken(
            accessToken = "testValidToken1",
            expiresIn = 5035365,
            tokenType = "bearer",
            receiveTimestamp = ofEpochSecond(1_686_895_955, 123_000_000),
        )
    }
}
