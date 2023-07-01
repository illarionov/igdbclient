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
