/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.parser.IgdbParser
import at.released.igdbclient.internal.parser.twitchTokenErrorResponseParser
import at.released.igdbclient.internal.parser.twitchTokenParser
import at.released.igdbclient.internal.twitch.TwitchCredentials
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher
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
