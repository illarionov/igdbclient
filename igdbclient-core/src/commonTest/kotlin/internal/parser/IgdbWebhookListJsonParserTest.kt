/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.containsExactly
import at.released.igdbclient.library.test.IgnoreAndroid
import at.released.igdbclient.model.IgdbWebhook
import at.released.igdbclient.model.IgdbWebhookId
import okio.Buffer
import kotlin.test.Test

@IgnoreAndroid
class IgdbWebhookListJsonParserTest {
    private val parser = IgdbParser::igdbWebhookListJsonParser

    @Test
    fun parser_should_return_correct_result_on_correct_data() {
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
        assertThat(webhookList)
            .containsExactly(
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
