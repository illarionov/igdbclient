/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalOkHttpApi::class)

package at.released.igdbclient.ktor.integration

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.integration.tests.BaseIgdbClientImplementationTest
import at.released.igdbclient.ktor.IgdbKtorEngine
import at.released.igdbclient.library.test.Fixtures
import at.released.igdbclient.library.test.IgdbClientConstants
import io.ktor.client.HttpClient
import mockwebserver3.SocketPolicy
import okhttp3.ExperimentalOkHttpApi
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

abstract class KtorBaseIgdbClientImplementationTest : BaseIgdbClientImplementationTest() {
    abstract fun createKtorClient(): HttpClient

    override fun createIgdbClient(baseUrl: String, authToken: String?): IgdbClient {
        return IgdbClient(IgdbKtorEngine) {
            this.baseUrl = baseUrl
            userAgent = "Test user agent"

            httpClient {
                httpClient = createKtorClient()
            }
            headers {
                append(IgdbClientConstants.Header.CLIENT_ID, Fixtures.TEST_CLIENT_ID)
                authToken?.let {
                    append(IgdbClientConstants.Header.AUTHORIZATION, "Bearer $it")
                }
                set("Header1", "HeaderValue1")
                append("Header2", "HeaderValue2")
                append("HeAdEr2", "HeaderValue22")
            }
        }
    }

    @Suppress("BACKTICKS_PROHIBITED")
    @ParameterizedTest
    @MethodSource(
        "at.released.igdbclient.integration.tests.BaseIgdbClientImplementationTest#networkErrorSocketPolicies",
    )
    @Disabled("Flacky and slow")
    override fun `Implementation should throw correct exception on network error`(policy: SocketPolicy) {
        super.`Implementation should throw correct exception on network error`(policy)
    }
}
