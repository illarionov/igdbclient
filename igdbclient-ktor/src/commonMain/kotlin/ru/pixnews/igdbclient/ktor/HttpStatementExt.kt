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

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.util.flattenEntries
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.BufferedSource
import okio.use
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.ktor.KtorIgdbConstants.DEFAULT_BUFFER_SIZE

internal suspend fun <T : Any, E : Any> HttpStatement.executeAsyncWithResult(
    query: ApicalypseQuery,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    successResponseParser: (ApicalypseQuery, BufferedSource) -> T,
    errorResponseParser: (ApicalypseQuery, BufferedSource) -> E,
): IgdbResult<T, E> {
    try {
        return this.execute { httpResponse ->
            httpResponse.requestTime
            withContext(backgroundDispatcher) {
                readHttpResponse(
                    query = query,
                    response = httpResponse,
                    successResponseParser = successResponseParser,
                    errorResponseParser = errorResponseParser,
                )
            }
        }
    } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
        return when (exception) {
            is IOException -> IgdbResult.Failure.NetworkFailure(exception)
            else -> IgdbResult.Failure.UnknownFailure(exception)
        }
    }
}

private suspend fun <T : Any, E : Any> readHttpResponse(
    query: ApicalypseQuery,
    response: HttpResponse,
    successResponseParser: (ApicalypseQuery, BufferedSource) -> T,
    errorResponseParser: (ApicalypseQuery, BufferedSource) -> E,
): IgdbResult<T, E> {
    val channel: ByteReadChannel = response.body()
    val responseBody = Buffer()

    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE)
        while (!packet.isEmpty) {
            val bytes = packet.readBytes()
            responseBody.write(bytes)
        }
    }

    @Suppress("MagicNumber")
    return when (response.status.value) {
        in 200..299 -> parseSuccessResponseBody(query, responseBody, successResponseParser)
        in 400..599 -> parseErrorResponseBody(query, response, responseBody, errorResponseParser)
        else -> IgdbResult.Failure.UnknownHttpCodeFailure(
            httpCode = response.status.value,
            httpMessage = response.status.description,
            rawResponseBody = responseBody.readByteArray(),
        )
    }
}

private fun <T : Any> parseSuccessResponseBody(
    query: ApicalypseQuery,
    responseBody: BufferedSource,
    parser: (ApicalypseQuery, BufferedSource) -> T,
): IgdbResult<T, Nothing> = try {
    val result = responseBody.use { parser(query, it) }
    IgdbResult.Success(result)
} catch (@Suppress("TooGenericExceptionCaught") exception: Throwable) {
    IgdbResult.Failure.ApiFailure(exception)
}

private fun <E : Any> parseErrorResponseBody(
    query: ApicalypseQuery,
    response: HttpResponse,
    responseBody: Buffer,
    httpErrorParser: (ApicalypseQuery, BufferedSource) -> E,
): IgdbResult.Failure.HttpFailure<E> {
    val responseBodyCopy = responseBody.snapshot()
    val errorMessage = try {
        httpErrorParser(query, responseBody)
    } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") exception: Exception) {
        null
    }

    return IgdbResult.Failure.HttpFailure(
        httpCode = response.status.value,
        httpMessage = response.status.description,
        response = errorMessage,
        rawResponseHeaders = response.headers.flattenEntries(),
        rawResponseBody = if (errorMessage != null) null else responseBodyCopy.toByteArray(),
    )
}
