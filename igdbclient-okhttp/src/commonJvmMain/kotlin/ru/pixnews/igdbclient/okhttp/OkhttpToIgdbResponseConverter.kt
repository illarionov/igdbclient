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
@file:Suppress("TooGenericExceptionCaught")

package ru.pixnews.igdbclient.okhttp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure.ApiFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.NetworkFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery
import java.io.IOException

internal suspend fun <T : Any, E : Any> Result<Response>.toIgdbResult(
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    successResponseParser: (BufferedSource) -> T,
    errorResponseParser: (BufferedSource) -> E,
): IgdbResult<T, E> = toIgdbResult(
    backgroundDispatcher = backgroundDispatcher,
    query = apicalypseQuery { },
    successResponseParser = { _, stream -> successResponseParser(stream) },
    errorResponseParser = { _, stream -> errorResponseParser(stream) },
)

internal suspend fun <T : Any, E : Any> Result<Response>.toIgdbResult(
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    query: ApicalypseQuery,
    successResponseParser: (ApicalypseQuery, BufferedSource) -> T,
    errorResponseParser: (ApicalypseQuery, BufferedSource) -> E,
): IgdbResult<T, E> = this.fold(
    onSuccess = { response ->
        withContext(backgroundDispatcher) {
            parseHttpResponse(query, response, successResponseParser, errorResponseParser)
        }
    },
    onFailure = { error ->
        if (error is IOException) {
            NetworkFailure(error)
        } else {
            UnknownFailure(error)
        }
    },
)

@Suppress("MagicNumber")
private fun <T : Any, E : Any> parseHttpResponse(
    query: ApicalypseQuery,
    response: Response,
    successResponseParser: (ApicalypseQuery, BufferedSource) -> T,
    errorResponseParser: (ApicalypseQuery, BufferedSource) -> E,
): IgdbResult<T, E> = when (response.code) {
    in 200..299 -> parseSuccessResponseBody(query, response, successResponseParser)
    in 400..599 -> parseErrorResponseBody(query, response, errorResponseParser)
    else -> UnknownHttpCodeFailure(
        httpCode = response.code,
        httpMessage = response.message,
        rawResponseBody = runCatching { response.body?.bytes() }.getOrNull(),
    )
}

private fun <T : Any> parseSuccessResponseBody(
    query: ApicalypseQuery,
    response: Response,
    parser: (ApicalypseQuery, BufferedSource) -> T,
): IgdbResult<T, Nothing> = try {
    val result = response.body!!.use { responseBody ->
        responseBody.source().use {
            parser(query, it)
        }
    }
    Success(result)
} catch (exception: Throwable) {
    ApiFailure(exception)
}

private fun <E : Any> parseErrorResponseBody(
    query: ApicalypseQuery,
    response: Response,
    httpErrorParser: (ApicalypseQuery, BufferedSource) -> E,
): HttpFailure<E> {
    val rawResponseBody = try {
        response.body?.bytes()
    } catch (ignore: Throwable) {
        null
    }
    val errorMessage = rawResponseBody?.let { rawResponse ->
        try {
            httpErrorParser(query, Buffer().write(rawResponse))
        } catch (@Suppress("SwallowedException") exception: Exception) {
            null
        }
    }

    return HttpFailure(
        httpCode = response.code,
        httpMessage = response.message,
        response = errorMessage,
        rawResponseHeaders = response.headers.toList(),
        rawResponseBody = if (errorMessage != null) null else rawResponseBody,
    )
}
