/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.okhttp

import com.squareup.wire.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenErrorResponseParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenParser
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher

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
