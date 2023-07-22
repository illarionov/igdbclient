/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal

import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.internal.twitch.TwitchTokenFetcher

@InternalIgdbClientApi
public interface IgdbHttpClient {
    public val requestExecutorFactory: (IgdbAuthToken?) -> RequestExecutor
    public val twitchTokenFetcherFactory: () -> TwitchTokenFetcher
}
