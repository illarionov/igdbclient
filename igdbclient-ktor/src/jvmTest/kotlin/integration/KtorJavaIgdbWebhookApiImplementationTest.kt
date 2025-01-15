/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor.integration

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

class KtorJavaIgdbWebhookApiImplementationTest : KtorBaseIgdbWebhookApiImplementationTest() {
    override fun createKtorClient(): HttpClient = HttpClient(Java) {
        applyTestDefaults()
    }
}
