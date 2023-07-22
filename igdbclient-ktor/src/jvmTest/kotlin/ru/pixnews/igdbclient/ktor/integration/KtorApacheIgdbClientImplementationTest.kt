/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor.integration

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5

class KtorApacheIgdbClientImplementationTest : KtorBaseIgdbClientImplementationTest() {
    override fun createKtorClient(): HttpClient = HttpClient(Apache5) {
        applyTestDefaults()
    }
}
