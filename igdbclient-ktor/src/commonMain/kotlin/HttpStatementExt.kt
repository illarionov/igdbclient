/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import ru.pixnews.igdbclient.ktor.KtorIgdbConstants.DEFAULT_BUFFER_SIZE

internal suspend fun <T : Any, E : Any> HttpStatement.executeAsyncWithResult(
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    successResponseParser: (BufferedSource) -> T,
    errorResponseParser: (BufferedSource) -> E,
): IgdbResult<T, E> {
    try {
        return this.execute { httpResponse ->
            httpResponse.requestTime
            withContext(backgroundDispatcher) {
                readHttpResponse(
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
    response: HttpResponse,
    successResponseParser: (BufferedSource) -> T,
    errorResponseParser: (BufferedSource) -> E,
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
        in 200..299 -> parseSuccessResponseBody(responseBody, successResponseParser)
        in 400..599 -> parseErrorResponseBody(response, responseBody, errorResponseParser)
        else -> IgdbResult.Failure.UnknownHttpCodeFailure(
            httpCode = response.status.value,
            httpMessage = response.status.description,
            rawResponseBody = responseBody.readByteArray(),
        )
    }
}

private fun <T : Any> parseSuccessResponseBody(
    responseBody: BufferedSource,
    parser: (BufferedSource) -> T,
): IgdbResult<T, Nothing> = try {
    val result = responseBody.use { parser(it) }
    IgdbResult.Success(result)
} catch (@Suppress("TooGenericExceptionCaught") exception: Throwable) {
    IgdbResult.Failure.ApiFailure(exception)
}

private fun <E : Any> parseErrorResponseBody(
    response: HttpResponse,
    responseBody: Buffer,
    httpErrorParser: (BufferedSource) -> E,
): IgdbResult.Failure.HttpFailure<E> {
    val responseBodyCopy = responseBody.snapshot()
    val errorMessage = try {
        httpErrorParser(responseBody)
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
