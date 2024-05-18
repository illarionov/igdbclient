/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.ktor

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.pixnews.igdbclient.dsl.IgdbClientDsl
import ru.pixnews.igdbclient.dsl.IgdbHttpEngineConfig

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
