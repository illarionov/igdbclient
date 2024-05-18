/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.model

import com.squareup.wire.Instant
import ru.pixnews.igdbclient.InternalIgdbClientApi

@InternalIgdbClientApi
public class TwitchToken(
    public val accessToken: String = "",
    public val expiresIn: Long = 0L,
    public val tokenType: String = "",
    public val receiveTimestamp: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as TwitchToken

        if (accessToken != other.accessToken) {
            return false
        }
        if (expiresIn != other.expiresIn) {
            return false
        }
        if (tokenType != other.tokenType) {
            return false
        }
        if (receiveTimestamp != other.receiveTimestamp) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = accessToken.hashCode()
        result = 31 * result + expiresIn.hashCode()
        result = 31 * result + tokenType.hashCode()
        result = 31 * result + (receiveTimestamp?.hashCode() ?: 0)
        return result
    }

    public fun copy(
        accessToken: String = this.accessToken,
        expiresIn: Long = this.expiresIn,
        tokenType: String = this.tokenType,
        receiveTimestamp: Instant? = this.receiveTimestamp,
    ): TwitchToken = TwitchToken(accessToken, expiresIn, tokenType, receiveTimestamp)

    internal companion object {
        internal fun TwitchToken.encode(): ByteArray = InternalTwitchTokenProto(
            access_token = this.accessToken,
            expires_in = this.expiresIn,
            token_type = this.tokenType,
            receive_timestamp = this.receiveTimestamp,
        ).encode()

        internal fun decode(bytes: ByteArray): TwitchToken = InternalTwitchTokenProto.ADAPTER.decode(bytes)
            .let {
                TwitchToken(
                    accessToken = it.access_token,
                    expiresIn = it.expires_in,
                    tokenType = it.token_type,
                    receiveTimestamp = it.receive_timestamp,
                )
            }
    }
}
