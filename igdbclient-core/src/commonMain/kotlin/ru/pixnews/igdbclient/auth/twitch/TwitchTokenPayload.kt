/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.auth.twitch

import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.model.TwitchToken.Companion.encode

public class TwitchTokenPayload(
    payload: ByteArray = ByteArray(0),
    public val version: Int = 1,
) {
    private val _payload: ByteArray = payload.copyOf()

    public val payload: ByteArray
        get() = _payload.copyOf()

    public fun isEmpty(): Boolean = _payload.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as TwitchTokenPayload

        if (version != other.version) {
            return false
        }
        if (!_payload.contentEquals(other._payload)) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + _payload.contentHashCode()
        return result
    }

    public companion object {
        public val NO_TOKEN: TwitchTokenPayload = TwitchTokenPayload()

        public operator fun invoke(twitchAccessToken: String): TwitchTokenPayload = TwitchTokenPayload(
            TwitchToken(twitchAccessToken).encode(),
        )

        /**
         * Returns raw twitch access token.
         *
         * It can be used to revoke a token on a twitch server when it is no longer needed.
         */
        public fun TwitchTokenPayload.getTwitchAccessToken(): String? = try {
            if (!isEmpty()) {
                TwitchToken.decode(this.payload).accessToken
            } else {
                null
            }
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }
    }
}
