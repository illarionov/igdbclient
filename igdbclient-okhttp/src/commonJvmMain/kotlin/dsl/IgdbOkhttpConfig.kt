/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp.dsl

import at.released.igdbclient.dsl.IgdbClientDsl
import at.released.igdbclient.dsl.IgdbHttpEngineConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Call

/**
 * Configuration of the [OkHttpClient][okhttp3.OkHttpClient] client used to make requests
 */
@IgdbClientDsl
public class IgdbOkhttpConfig : IgdbHttpEngineConfig {
    /**
     * Allows you to specify a pre-configured [OkHttp Call.Factory][Call.Factory] for sending requests
     */
    public var callFactory: Call.Factory? = null

    /**
     * Сoroutine dispatcher used for deserialization of received responses
     */
    public var backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
}
