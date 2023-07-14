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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of the [TwitchTokenStorage], which stores the received token only in memory.
 */
public class InMemoryTwitchTokenStorage(
    private var token: TwitchTokenPayload = TwitchTokenPayload.NO_TOKEN,
) : TwitchTokenStorage {
    private val lock = Mutex()

    override suspend fun getToken(): TwitchTokenPayload = lock.withLock { token }

    override suspend fun updateToken(oldToken: TwitchTokenPayload, newToken: TwitchTokenPayload): Boolean {
        return lock.withLock {
            if (oldToken == token) {
                token = newToken
                true
            } else {
                false
            }
        }
    }
}
