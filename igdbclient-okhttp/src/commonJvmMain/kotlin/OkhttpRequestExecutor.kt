/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.internal.IgdbRequest
import at.released.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import at.released.igdbclient.internal.IgdbRequest.DeleteRequest
import at.released.igdbclient.internal.IgdbRequest.FormUrlEncodedPostRequest
import at.released.igdbclient.internal.IgdbRequest.GetRequest
import at.released.igdbclient.internal.RequestExecutor
import at.released.igdbclient.internal.model.IgdbAuthToken
import at.released.igdbclient.internal.parser.IgdbParser
import at.released.igdbclient.internal.parser.igdbErrorResponseParser
import at.released.igdbclient.okhttp.OkhttpIgdbConstants.Header
import at.released.igdbclient.okhttp.OkhttpIgdbConstants.MediaType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Call.Factory
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource

/**
 * Implementation of th [RequestExecutor] based on [okhttp3.OkHttpClient].
 */
internal class OkhttpRequestExecutor(
    private val callFactory: Factory,
    private val baseUrl: HttpUrl,
    private val token: IgdbAuthToken? = null,
    private val userAgent: String? = null,
    private val headers: Map<String, List<String>> = emptyMap(),
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpErrorJsonParser: (BufferedSource) -> IgdbHttpErrorResponse = IgdbParser::igdbErrorResponseParser,
) : RequestExecutor {
    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        val okhttpRequest: Request
        val successResponseParser: (BufferedSource) -> Any
        when (request) {
            is ApicalypsePostRequest<*> ->
                @Suppress("UNCHECKED_CAST")
                return postApicalypseRequest(
                    request.path,
                    request.query,
                    request.successResponseParser,
                ) as IgdbResult<T, IgdbHttpErrorResponse>

            is GetRequest<*> -> {
                val url = baseUrl.newBuilder().apply {
                    addPathSegments(request.path)
                    request.queryParameters.forEach { addQueryParameter(it.key, it.value) }
                }.build()
                okhttpRequest = createIgdbOkhttpRequestBuilder(url).get().build()
                successResponseParser = request.successResponseParser
            }

            is FormUrlEncodedPostRequest<*> -> {
                val url = baseUrl.newBuilder().apply {
                    addPathSegments(request.path)
                    request.queryParameters.forEach { addQueryParameter(it.key, it.value) }
                }.build()
                val body = FormBody.Builder().apply {
                    request.formUrlEncodedParameters.forEach { add(it.key, it.value) }
                }.build()
                okhttpRequest = createIgdbOkhttpRequestBuilder(url).post(body).build()
                successResponseParser = request.successResponseParser
            }

            is DeleteRequest<*> -> {
                val url = baseUrl.newBuilder().apply {
                    addPathSegments(request.path)
                    request.queryParameters.forEach { addQueryParameter(it.key, it.value) }
                }.build()

                okhttpRequest = createIgdbOkhttpRequestBuilder(
                    url = url,
                    acceptMediaType = MediaType.APPLICATION_JSON,
                ).delete().build()

                successResponseParser = request.successResponseParser
            }
        }

        @Suppress("UNCHECKED_CAST")
        return callFactory
            .newCall(okhttpRequest)
            .executeAsyncWithResult()
            .toIgdbResult(
                backgroundDispatcher = backgroundDispatcher,
                successResponseParser = successResponseParser,
                errorResponseParser = httpErrorJsonParser,
            ) as IgdbResult<T, IgdbHttpErrorResponse>
    }

    private suspend fun <T : Any> postApicalypseRequest(
        path: String,
        query: ApicalypseQuery,
        successResponseParser: (BufferedSource) -> T,
    ): IgdbResult<T, IgdbHttpErrorResponse> {
        val url = baseUrl.newBuilder().addPathSegments(path).build()
        val body = query.toString().toRequestBody(MediaType.TEXT_PLAIN)
        val okhttpRequest = createIgdbOkhttpRequestBuilder(
            url = url,
            acceptMediaType = MediaType.APPLICATION_PROTOBUF,
        ).post(body).build()

        return callFactory
            .newCall(okhttpRequest)
            .executeAsyncWithResult()
            .toIgdbResult(
                successResponseParser = successResponseParser,
                errorResponseParser = httpErrorJsonParser,
                backgroundDispatcher = backgroundDispatcher,
            )
    }

    private fun createIgdbOkhttpRequestBuilder(
        url: HttpUrl,
        acceptMediaType: String = MediaType.APPLICATION_JSON,
    ): Builder = Builder().apply {
        url(url)
        header("Accept", acceptMediaType)
        userAgent?.let {
            header("User-Agent", it)
        }
        token?.let {
            header(Header.CLIENT_ID, it.clientId)
            header(Header.AUTHORIZATION, "Bearer ${it.token}")
        }
        addUserDefinedHeaders()
    }

    private fun Builder.addUserDefinedHeaders() = headers.forEach { (headerName, values) ->
        if (headerName.isSingleValueHeader()) {
            values.firstOrNull()?.let {
                header(headerName, it)
            }
        } else {
            values.forEach { addHeader(headerName, it) }
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
