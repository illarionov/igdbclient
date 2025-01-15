/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.model

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.dsl.IgdbHttpEngineConfig
import at.released.igdbclient.dsl.RetryPolicy
import at.released.igdbclient.dsl.TwitchAuthConfig

@InternalIgdbClientApi
public class IgdbClientConfig<C : IgdbHttpEngineConfig>(
    public val baseUrl: String,
    public val userAgent: String?,
    public val twitchAuthConfig: TwitchAuthConfig,
    public val httpClientConfig: C.() -> Unit,
    public val retryPolicy: RetryPolicy,
    public val headers: Map<String, List<String>>,
)
