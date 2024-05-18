/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
