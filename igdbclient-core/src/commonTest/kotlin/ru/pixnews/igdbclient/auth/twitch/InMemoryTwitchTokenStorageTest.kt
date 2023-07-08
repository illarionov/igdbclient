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
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InMemoryTwitchTokenStorageTest {
    @Test
    fun getToken_initially_should_return_dummy_token_value_by_default() = runTest {
        val storage = InMemoryTwitchTokenStorage()
        val token = storage.getToken()
        assertThat(token).isEqualTo(TwitchTokenPayload.NO_TOKEN)
    }

    @Test
    fun getToken_should_return_token_specified_in_the_constructor() = runTest {
        val storage = InMemoryTwitchTokenStorage(TEST_TOKEN)
        val token = storage.getToken()
        assertThat(token).isEqualTo(TEST_TOKEN)
    }

    @Test
    fun updateToken_should_correctly_update_token() = runTest {
        val storage = InMemoryTwitchTokenStorage()

        val isUpdated = storage.updateToken(TwitchTokenPayload.NO_TOKEN, TEST_TOKEN)
        val newToken = storage.getToken()

        assertThat(isUpdated).isTrue()
        assertThat(newToken).isEqualTo(TEST_TOKEN)
    }

    @Test
    fun updateToken_should_not_update_token_if_original_token_not_match() = runTest {
        val initialToken = TwitchTokenPayload(byteArrayOf(4, 5, 6))
        val storage = InMemoryTwitchTokenStorage(initialToken)

        val isUpdated = storage.updateToken(TwitchTokenPayload.NO_TOKEN, TEST_TOKEN)
        val newToken = storage.getToken()

        assertThat(isUpdated).isFalse()
        assertThat(newToken).isEqualTo(initialToken)
    }

    companion object {
        val TEST_TOKEN = TwitchTokenPayload(byteArrayOf(1, 2, 3))
    }
}
