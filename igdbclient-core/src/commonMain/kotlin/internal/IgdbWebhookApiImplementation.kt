/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import at.released.igdbclient.IgdbEndpoint
import at.released.igdbclient.IgdbResult
import at.released.igdbclient.IgdbWebhookApi
import at.released.igdbclient.IgdbWebhookApi.WebhookMethod
import at.released.igdbclient.IgdbWebhookApi.WebhookMethod.CREATE
import at.released.igdbclient.IgdbWebhookApi.WebhookMethod.DELETE
import at.released.igdbclient.IgdbWebhookApi.WebhookMethod.UPDATE
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.internal.IgdbRequest.DeleteRequest
import at.released.igdbclient.internal.IgdbRequest.FormUrlEncodedPostRequest
import at.released.igdbclient.internal.IgdbRequest.GetRequest
import at.released.igdbclient.internal.parser.IgdbParser
import at.released.igdbclient.internal.parser.igdbWebhookListJsonParser
import at.released.igdbclient.model.IgdbWebhook
import at.released.igdbclient.model.IgdbWebhookId

internal class IgdbWebhookApiImplementation(
    val requestExecutor: RequestExecutor,
) : IgdbWebhookApi {
    override suspend fun registerWebhook(
        endpoint: IgdbEndpoint<*>,
        url: String,
        method: WebhookMethod,
        secret: String,
    ): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse> {
        val request = FormUrlEncodedPostRequest(
            path = endpoint.webhookPath,
            formUrlEncodedParameters = mapOf(
                "url" to url,
                "method" to method.igdbApiId,
                "secret" to secret,
            ),
            successResponseParser = IgdbParser::igdbWebhookListJsonParser,
        )
        return requestExecutor(request)
    }

    override suspend fun getAllWebhooks(): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse> {
        val request = GetRequest(
            path = "webhooks",
            queryParameters = mapOf(),
            successResponseParser = IgdbParser::igdbWebhookListJsonParser,
        )
        return requestExecutor(request)
    }

    override suspend fun getWebhook(webhookId: IgdbWebhookId): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse> {
        val request = GetRequest(
            path = "webhooks/${webhookId.value}",
            queryParameters = mapOf(),
            successResponseParser = IgdbParser::igdbWebhookListJsonParser,
        )
        return requestExecutor(request)
    }

    override suspend fun deleteWebhook(webhookId: IgdbWebhookId): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse> {
        val request = DeleteRequest(
            path = "webhooks/${webhookId.value}",
            queryParameters = mapOf(),
            successResponseParser = IgdbParser::igdbWebhookListJsonParser,
        )
        return requestExecutor(request)
    }

    override suspend fun testWebhook(
        endpoint: IgdbEndpoint<*>,
        webhookId: IgdbWebhookId,
        entityId: String,
    ): IgdbResult<String, IgdbHttpErrorResponse> {
        val webhookPath = endpoint.getTestWebhookPath(webhookId)
        val request = FormUrlEncodedPostRequest(
            path = webhookPath,
            formUrlEncodedParameters = mapOf(),
            queryParameters = mapOf(
                "entityId" to entityId,
            ),
            successResponseParser = { source -> source.readByteString().utf8() },
        )
        return requestExecutor(request)
    }

    private companion object {
        private val WebhookMethod.igdbApiId: String
            get() = when (this) {
                CREATE -> "create"
                DELETE -> "delete"
                UPDATE -> "update"
            }
    }
}
