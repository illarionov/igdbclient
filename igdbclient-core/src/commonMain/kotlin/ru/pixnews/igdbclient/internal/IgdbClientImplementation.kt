/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal

import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbWebhookApi
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest

internal class IgdbClientImplementation(
    private val requestExecutor: RequestExecutor,
) : IgdbClient {
    override val webhookApi: IgdbWebhookApi by lazy {
        IgdbWebhookApiImplementation(requestExecutor)
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
