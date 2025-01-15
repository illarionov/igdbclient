/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught")

package at.released.igdbclient.okhttp

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.IgdbResult.Failure.ApiFailure
import at.released.igdbclient.IgdbResult.Failure.HttpFailure
import at.released.igdbclient.IgdbResult.Failure.NetworkFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import at.released.igdbclient.IgdbResult.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import java.io.IOException

internal suspend fun <T : Any, E : Any> Result<Response>.toIgdbResult(
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    successResponseParser: (BufferedSource) -> T,
    errorResponseParser: (BufferedSource) -> E,
): IgdbResult<T, E> = this.fold(
    onSuccess = { response ->
        withContext(backgroundDispatcher) {
            parseHttpResponse(response, successResponseParser, errorResponseParser)
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
    response: Response,
    successResponseParser: (BufferedSource) -> T,
    errorResponseParser: (BufferedSource) -> E,
): IgdbResult<T, E> = when (response.code) {
    in 200..299 -> parseSuccessResponseBody(response, successResponseParser)
    in 400..599 -> parseErrorResponseBody(response, errorResponseParser)
    else -> UnknownHttpCodeFailure(
        httpCode = response.code,
        httpMessage = response.message,
        rawResponseBody = runCatching { response.body?.bytes() }.getOrNull(),
    )
}

private fun <T : Any> parseSuccessResponseBody(
    response: Response,
    parser: (BufferedSource) -> T,
): IgdbResult<T, Nothing> = try {
    val result = response.body!!.use { responseBody ->
        responseBody.source().use {
            parser(it)
        }
    }
    Success(result)
} catch (exception: Throwable) {
    ApiFailure(exception)
}

private fun <E : Any> parseErrorResponseBody(
    response: Response,
    httpErrorParser: (BufferedSource) -> E,
): HttpFailure<E> {
    val rawResponseBody = try {
        response.body?.bytes()
    } catch (ignore: Throwable) {
        null
    }
    val errorMessage = rawResponseBody?.let { rawResponse ->
        try {
            httpErrorParser(Buffer().write(rawResponse))
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
