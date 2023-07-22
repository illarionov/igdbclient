/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient

public sealed interface IgdbResult<out R : Any, out E : Any> {
    /**
     * 2xx response with successfully parsed body
     */
    public class Success<R : Any>(
        public val value: R,
    ) : IgdbResult<R, Nothing>

    public sealed class Failure<E : Any> : IgdbResult<Nothing, E> {
        /**
         * Any network error, no HTTP response received
         */
        public class NetworkFailure(
            public val error: Throwable,
        ) : Failure<Nothing>()

        /**
         * 4xx - 5xx HTTP errors
         */
        public class HttpFailure<E : Any>(
            public val httpCode: Int,
            public val httpMessage: String,
            public val response: E?,
            public val rawResponseHeaders: List<Pair<String, String>>?,
            public val rawResponseBody: ByteArray?,
        ) : Failure<E>()

        /**
         * Other HTTP errors
         */
        public class UnknownHttpCodeFailure(
            public val httpCode: Int,
            public val httpMessage: String,
            public val rawResponseBody: ByteArray?,
        ) : Failure<Nothing>()

        /**
         * 2xx HTTP response with unparsable body
         */
        public class ApiFailure(
            public val error: Throwable?,
        ) : Failure<Nothing>()

        /**
         * Other failures
         */
        public class UnknownFailure(
            public val error: Throwable?,
        ) : Failure<Nothing>()
    }
}
