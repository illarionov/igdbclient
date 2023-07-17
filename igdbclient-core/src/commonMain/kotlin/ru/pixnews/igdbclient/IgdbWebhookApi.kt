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

import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

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
