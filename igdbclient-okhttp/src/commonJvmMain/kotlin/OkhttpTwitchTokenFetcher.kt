/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.parser.IgdbParser
import at.released.igdbclient.internal.parser.twitchTokenErrorResponseParser
import at.released.igdbclient.internal.parser.twitchTokenParser
import at.released.igdbclient.internal.twitch.TwitchCredentials
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher
import com.squareup.wire.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okio.BufferedSource

/**
 * Twitch Client Credentials Grant Flow fetcher
 */
internal class OkhttpTwitchTokenFetcher(
    private val callFactory: Call.Factory,
    private val baseUrl: HttpUrl = OkhttpIgdbConstants.TWITCH_AUTH_URL,
    private val userAgent: String? = null,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val twitchTokenParser: (BufferedSource) -> TwitchToken = IgdbParser::twitchTokenParser,
    private val twitchErrorResponseParser: (BufferedSource) -> TwitchErrorResponse =
        IgdbParser::twitchTokenErrorResponseParser,
    private val tokenTimestampSource: () -> Long = System::currentTimeMillis,
) : TwitchTokenFetcher {
    override suspend fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse> {
        val body = FormBody.Builder()
            .add("client_id", credentials.clientId)
            .add("client_secret", credentials.clientSecret)
            .add("grant_type", CLIENT_CREDENTIALS_GRANT_TYPE)
            .build()
        val request = Request.Builder().apply {
            url(baseUrl)
            addHeader("Accept", OkhttpIgdbConstants.MediaType.APPLICATION_JSON)
            userAgent?.let {
                addHeader("User-Agent", it)
            }
            post(body)
        }.build()

        val tokenReceivedTimestamp = tokenTimestampSource()
        return callFactory
            .newCall(request)
            .executeAsyncWithResult()
            .toIgdbResult(
                successResponseParser = { inputStream ->
                    @Suppress("MagicNumber")
                    twitchTokenParser(inputStream).copy(
                        receiveTimestamp = Instant.ofEpochSecond(
                            tokenReceivedTimestamp / 1000,
                            tokenReceivedTimestamp % 1000L * 1_000_000L,
                        ),
                    )
                },
                errorResponseParser = twitchErrorResponseParser,
                backgroundDispatcher = backgroundDispatcher,
            )
    }

    private companion object {
        const val CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials"
    }
}
