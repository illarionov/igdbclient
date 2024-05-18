/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.auth.twitch

public interface TwitchTokenStorage {
    public suspend fun getToken(): TwitchTokenPayload

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    public suspend fun updateToken(oldToken: TwitchTokenPayload, newToken: TwitchTokenPayload): Boolean
}
