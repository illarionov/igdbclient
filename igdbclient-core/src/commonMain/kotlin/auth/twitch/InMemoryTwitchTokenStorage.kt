/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.auth.twitch

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
