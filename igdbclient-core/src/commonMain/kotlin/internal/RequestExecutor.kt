/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.error.IgdbHttpErrorResponse
import okio.BufferedSource

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
