/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.IgdbAuthToken
import at.released.igdbclient.internal.twitch.TwitchTokenFetcher

@InternalIgdbClientApi
public interface IgdbHttpClient {
    public val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor
    public val twitchTokenFetcherFactory: () -> TwitchTokenFetcher
}
