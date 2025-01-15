/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.auth.twitch

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import at.released.igdbclient.auth.twitch.TwitchTokenPayload.Companion.getTwitchAccessToken
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.model.TwitchToken.Companion.encode
import com.squareup.wire.ofEpochSecond
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
