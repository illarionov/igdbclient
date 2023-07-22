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

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.igdbErrorResponseParser
import ru.pixnews.igdbclient.ktor.KtorIgdbConstants.Header

internal class KtorRequestExecutor(
    private val httpClient: HttpClient,
    private val baseUrl: URLBuilder,
    private val token: IgdbAuthToken? = null,
    private val userAgent: String? = null,
    private val headers: Map<String, List<String>> = emptyMap(),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpErrorJsonParser: (BufferedSource) -> IgdbHttpErrorResponse = IgdbParser::igdbErrorResponseParser,
) : RequestExecutor {
    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        val apicalypseQuery: ApicalypseQuery
        val statement: HttpStatement
        val successResponseParser: (BufferedSource) -> T

        when (request) {
            is IgdbRequest.ApicalypsePostRequest<*> -> {
                apicalypseQuery = request.query
                @Suppress("UNCHECKED_CAST")
                successResponseParser = request.successResponseParser as (BufferedSource) -> T
                val url = buildUrl(request.path, null)
                statement = httpClient.prepareRequest(url) {
                    method = HttpMethod.Post
                    setupHeaders(ContentType.Application.ProtoBuf)
                    contentType(ContentType.Text.Plain)
                    setBody(apicalypseQuery.toString())
                }
            }

            is IgdbRequest.DeleteRequest<*> -> {
                apicalypseQuery = apicalypseQuery { }
                @Suppress("UNCHECKED_CAST")
                successResponseParser = request.successResponseParser as (BufferedSource) -> T
                val url = buildUrl(request.path, request.queryParameters)
                statement = httpClient.prepareRequest(url) {
                    method = HttpMethod.Delete
                    setupHeaders(ContentType.Application.Json)
                }
            }

            is IgdbRequest.FormUrlEncodedPostRequest<*> -> {
                apicalypseQuery = apicalypseQuery { }
                @Suppress("UNCHECKED_CAST")
                successResponseParser = request.successResponseParser as (BufferedSource) -> T
                val url = buildUrl(request.path, request.queryParameters)
                val formParameters = parameters {
                    request.formUrlEncodedParameters.forEach { this.append(it.key, it.value) }
                }
                statement = httpClient.prepareRequest(url) {
                    method = HttpMethod.Post
                    setupHeaders(ContentType.Application.Json)
                    setBody(FormDataContent(formParameters))
                }
            }

            is IgdbRequest.GetRequest<*> -> {
                apicalypseQuery = apicalypseQuery { }
                @Suppress("UNCHECKED_CAST")
                successResponseParser = request.successResponseParser as (BufferedSource) -> T
                val url = buildUrl(request.path, request.queryParameters)
                statement = httpClient.prepareRequest(url) {
                    method = HttpMethod.Get
                    setupHeaders(ContentType.Application.Json)
                }
            }
        }

        return statement.executeAsyncWithResult(
            backgroundDispatcher = backgroundDispatcher,
            successResponseParser = successResponseParser,
            errorResponseParser = httpErrorJsonParser,
        )
    }

    private fun buildUrl(
        pathSegments: String,
        queryParameters: Map<String, String>?,
    ): Url = URLBuilder(baseUrl).apply {
        appendPathSegments(pathSegments)
        queryParameters?.forEach {
            parameters.append(it.key, it.value)
        }
    }.build()

    private fun HttpRequestBuilder.setupHeaders(
        acceptMediaType: ContentType = ContentType.Application.Json,
    ) {
        this.headers {
            set(HttpHeaders.Accept, acceptMediaType.toString())
            this@KtorRequestExecutor.userAgent?.let {
                set(HttpHeaders.UserAgent, it)
            }
            token?.let {
                set(Header.CLIENT_ID, it.clientId)
                set(HttpHeaders.Authorization, "Bearer ${it.token}")
            }
            addUserDefinedHeaders()
        }
    }

    private fun HeadersBuilder.addUserDefinedHeaders() = headers.forEach { (headerName, values) ->
        if (headerName.isSingleValueHeader()) {
            values.firstOrNull()?.let {
                this[headerName] = it
            }
        } else {
            appendAll(headerName, values)
        }
    }

    private companion object {
        private val SINGLE_VALUE_HEADERS = setOf(
            Header.CLIENT_ID,
            Header.AUTHORIZATION,
        )

        private fun String.isSingleValueHeader(): Boolean = SINGLE_VALUE_HEADERS.any {
            it.equals(this, ignoreCase = true)
        }
    }
}
