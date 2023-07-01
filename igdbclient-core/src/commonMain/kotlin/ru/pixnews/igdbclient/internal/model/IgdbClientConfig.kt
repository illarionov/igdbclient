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
package ru.pixnews.igdbclient.internal.model

import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.dsl.IgdbHttpEngineConfig
import ru.pixnews.igdbclient.dsl.RetryPolicy
import ru.pixnews.igdbclient.dsl.TwitchAuthConfig

@InternalIgdbClientApi
public class IgdbClientConfig<C : IgdbHttpEngineConfig>(
    public val baseUrl: String,
    public val userAgent: String?,
    public val twitchAuthConfig: TwitchAuthConfig,
    public val httpClientConfig: C.() -> Unit,
    public val retryPolicy: RetryPolicy,
    public val headers: Map<String, List<String>>,
)
