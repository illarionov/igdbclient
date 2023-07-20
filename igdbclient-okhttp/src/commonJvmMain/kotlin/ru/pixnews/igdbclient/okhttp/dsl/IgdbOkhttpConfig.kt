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
