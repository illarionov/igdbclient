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
package ru.pixnews.igdbclient.internal.parser

import io.kotest.matchers.collections.shouldContainInOrder
import okio.Buffer
import org.junit.jupiter.api.Test
import ru.pixnews.igdbclient.model.IgdbWebhook
import ru.pixnews.igdbclient.model.IgdbWebhookId

class IgdbWebhookListJsonParserTest {
    private val parser = IgdbParser::igdbWebhookListJsonParser

    @Test
    fun `Parser should return correct result on correct data`() {
        val responseText = """
            [
              {
                "id": 7133,
                "url": "https://example.com/games/webhook",
                "category": 625691411,
                "sub_category": 0,
                "active": false,
                "number_of_retries": 5,
                "api_key": "api_key_1",
                "secret": "webhook_secret_1",
                "created_at": 1687847078,
                "updated_at": 1687849689
              },
              {
                "id": 7135,
                "url": "https://example.com/games/2/webhook",
                "category": 625691412,
                "sub_category": 2,
                "active": true,
                "number_of_retries": 0,
                "api_key": "api_key2",
                "secret": "webhook_secret_2",
                "created_at": 1688013039,
                "updated_at": 1688013039
              }
            ]
        """.trimIndent()
        val webhookList = parser(Buffer().write(responseText.encodeToByteArray()))
        webhookList.shouldContainInOrder(
            IgdbWebhook(
                id = IgdbWebhookId("7133"),
                url = "https://example.com/games/webhook",
                category = "625691411",
                subCategory = "0",
                active = false,
                numberOfRetries = 5,
                apiKey = "api_key_1",
                secret = "webhook_secret_1",
                createdAt = 1_687_847_078,
                updatedAt = 1_687_849_689,
            ),
            IgdbWebhook(
                id = IgdbWebhookId("7135"),
                url = "https://example.com/games/2/webhook",
                category = "625691412",
                subCategory = "2",
                active = true,
                numberOfRetries = 0,
                apiKey = "api_key2",
                secret = "webhook_secret_2",
                createdAt = 1_688_013_039,
                updatedAt = 1_688_013_039,
            ),
        )
    }
}
