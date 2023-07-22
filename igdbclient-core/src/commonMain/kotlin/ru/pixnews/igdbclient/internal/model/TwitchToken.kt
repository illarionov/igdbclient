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
