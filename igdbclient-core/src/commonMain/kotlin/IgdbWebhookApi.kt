/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient

import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.model.IgdbWebhook
import at.released.igdbclient.model.IgdbWebhookId

/**
 * IgdbWebhookApi allows you to manage webhooks using the IGDB Webhook API
 *
 * See [https://api-docs.igdb.com/#webhooks](https://api-docs.igdb.com/#webhooks)
 */
public interface IgdbWebhookApi {
    /**
     * Registers a new webhook.
     */
    public suspend fun registerWebhook(
        endpoint: IgdbEndpoint<*>,
        url: String,
        method: WebhookMethod,
        secret: String,
    ): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse>

    /**
     * Returns all registered webhooks.
     */
    public suspend fun getAllWebhooks(): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse>

    /**
     * Returns information about a specific webhook.
     */
    public suspend fun getWebhook(webhookId: IgdbWebhookId): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse>

    /**
     * Removes a registered webhook.
     *
     * Returns information about the removed webhook as confirmation.
     */
    public suspend fun deleteWebhook(webhookId: IgdbWebhookId): IgdbResult<List<IgdbWebhook>, IgdbHttpErrorResponse>

    /**
     * Allows you to test a registered webhook.
     *
     * Sends the object with id [entityId] to the registered webhook [webhookId].
     */
    public suspend fun testWebhook(
        endpoint: IgdbEndpoint<*>,
        webhookId: IgdbWebhookId,
        entityId: String,
    ): IgdbResult<String, IgdbHttpErrorResponse>

    /**
     * The type of data change event for which a subscription will be created and notifications will be sent to the
     * webhook url
     */
    public enum class WebhookMethod {
        /**
         * Events when new items are created
         */
        CREATE,

        /**
         * Events when items are removed
         */
        DELETE,

        /**
         * Events when items are updated
         */
        UPDATE,
    }
}
