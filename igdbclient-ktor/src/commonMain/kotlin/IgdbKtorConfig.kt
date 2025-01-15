/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor

import at.released.igdbclient.dsl.IgdbClientDsl
import at.released.igdbclient.dsl.IgdbHttpEngineConfig
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration of the [Ktor HttpClient][io.ktor.client.HttpClient] used to make requests
 */
@IgdbClientDsl
public class IgdbKtorConfig : IgdbHttpEngineConfig {
    /**
     * Allows you to specify a pre-configured [HttpClient][io.ktor.client.HttpClient] for sending requests
     */
    public var httpClient: HttpClient? = null

    /**
     * Сoroutine dispatcher used to receive and deserialize responses
     */
    public var backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
}
