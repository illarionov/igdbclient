/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal

import okio.BufferedSource
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse

@InternalIgdbClientApi
public interface RequestExecutor {
    public suspend operator fun <T : Any> invoke(
        request: IgdbRequest,
    ): IgdbResult<T, IgdbHttpErrorResponse>
}

@InternalIgdbClientApi
public sealed class IgdbRequest {
    @InternalIgdbClientApi
    public class ApicalypsePostRequest<out T : Any>(
        public val path: String,
        public val query: ApicalypseQuery,
        public val successResponseParser: (BufferedSource) -> T,
    ) : IgdbRequest()

    @InternalIgdbClientApi
    public class FormUrlEncodedPostRequest<out T : Any>(
        public val path: String,
        public val queryParameters: Map<String, String> = mapOf(),
        public val formUrlEncodedParameters: Map<String, String> = mapOf(),
        public val successResponseParser: (BufferedSource) -> T,
    ) : IgdbRequest()

    @InternalIgdbClientApi
    public class GetRequest<out T : Any>(
        public val path: String,
        public val queryParameters: Map<String, String> = mapOf(),
        public val successResponseParser: (BufferedSource) -> T,
    ) : IgdbRequest()

    @InternalIgdbClientApi
    public class DeleteRequest<out T : Any>(
        public val path: String,
        public val queryParameters: Map<String, String> = mapOf(),
        public val successResponseParser: (BufferedSource) -> T,
    ) : IgdbRequest()
}
