/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.okhttp.dsl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import ru.pixnews.igdbclient.dsl.IgdbClientDsl
import ru.pixnews.igdbclient.dsl.IgdbHttpEngineConfig

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
