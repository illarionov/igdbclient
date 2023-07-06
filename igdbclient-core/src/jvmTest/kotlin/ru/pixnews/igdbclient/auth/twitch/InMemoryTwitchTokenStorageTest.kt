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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload.Companion.NO_TOKEN
import ru.pixnews.igdbclient.library.test.MainCoroutineExtension

class InMemoryTwitchTokenStorageTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()

    @Test
    fun `getToken initially should return dummy token value by default`() = coroutinesExt.runTest {
        val storage = InMemoryTwitchTokenStorage()
        val token = storage.getToken()
        token shouldBe NO_TOKEN
    }

    @Test
    fun `getToken should return token specified in the constructor`() = coroutinesExt.runTest {
        val storage = InMemoryTwitchTokenStorage(TEST_TOKEN)
        val token = storage.getToken()
        token shouldBe TEST_TOKEN
    }

    @Test
    fun `updateToken should correctly update token`() = coroutinesExt.runTest {
        val storage = InMemoryTwitchTokenStorage()

        val isUpdated = storage.updateToken(NO_TOKEN, TEST_TOKEN)
        val newToken = storage.getToken()

        isUpdated shouldBe true
        newToken shouldBe TEST_TOKEN
    }

    @Test
    fun `updateToken should not update token if original token not match`() = coroutinesExt.runTest {
        val initialToken = TwitchTokenPayload(byteArrayOf(4, 5, 6))
        val storage = InMemoryTwitchTokenStorage(initialToken)

        val isUpdated = storage.updateToken(NO_TOKEN, TEST_TOKEN)
        val newToken = storage.getToken()

        isUpdated shouldBe false
        newToken shouldBe initialToken
    }

    companion object {
        val TEST_TOKEN = TwitchTokenPayload(byteArrayOf(1, 2, 3))
    }
}
