/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.IgdbDumpApi
import at.released.igdbclient.IgdbEndpoint
import at.released.igdbclient.IgdbResult
import at.released.igdbclient.IgdbWebhookApi
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.internal.IgdbRequest.ApicalypsePostRequest

internal class IgdbClientImplementation(
    private val requestExecutor: RequestExecutor,
) : IgdbClient {
    override val webhookApi: IgdbWebhookApi by lazy {
        IgdbWebhookApiImplementation(requestExecutor)
    }
    override val dumpApi: IgdbDumpApi by lazy {
        IgdbDumpApiImplementation(requestExecutor)
    }

    override suspend fun <T : Any> execute(
        endpoint: IgdbEndpoint<T>,
        query: ApicalypseQuery,
    ): IgdbResult<T, IgdbHttpErrorResponse> = requestExecutor(
        ApicalypsePostRequest(
            path = endpoint.protobufPath,
            query = query,
            successResponseParser = { source -> endpoint.resultParser(query, source) },
        ),
    )
}
