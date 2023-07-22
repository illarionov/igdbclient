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
package ru.pixnews.igdbclient.ktor

import com.squareup.wire.ofEpochSecond
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.parameters
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.CoroutineDispatcher
import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenErrorResponseParser
import ru.pixnews.igdbclient.internal.parser.twitchTokenParser
import ru.pixnews.igdbclient.internal.twitch.TwitchCredentials
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher

internal class KtorTwitchTokenFetcher(
    private val httpClient: HttpClient,
    private val baseUrl: Url = KtorIgdbConstants.TWITCH_AUTH_URL,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val userAgent: String? = null,
    private val twitchTokenParser: (BufferedSource) -> TwitchToken = IgdbParser::twitchTokenParser,
    private val twitchErrorResponseParser: (BufferedSource) -> TwitchErrorResponse =
        IgdbParser::twitchTokenErrorResponseParser,
    private val tokenTimestampSource: () -> Long = ::getTimeMillis,
) : TwitchTokenFetcher {
    override suspend fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse> {
        val formParameters = parameters {
            append("client_id", credentials.clientId)
            append("client_secret", credentials.clientSecret)
            append("grant_type", CLIENT_CREDENTIALS_GRANT_TYPE)
        }
        val statement = httpClient.prepareRequest(baseUrl) {
            method = HttpMethod.Post
            accept(Application.Json)
            userAgent?.let {
                headers.append("User-Agent", it)
            }
            setBody(FormDataContent(formParameters))
        }
        val tokenReceivedTimestamp = tokenTimestampSource()
        return statement
            .executeAsyncWithResult(
                successResponseParser = { inputStream ->
                    @Suppress("MagicNumber")
                    twitchTokenParser(inputStream).copy(
                        receiveTimestamp = ofEpochSecond(
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
