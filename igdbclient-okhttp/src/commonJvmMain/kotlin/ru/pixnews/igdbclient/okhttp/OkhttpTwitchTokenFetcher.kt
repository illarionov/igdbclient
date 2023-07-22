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
