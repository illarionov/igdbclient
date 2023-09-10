/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.twitch

import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.internal.model.TwitchToken

@InternalIgdbClientApi
public interface TwitchTokenFetcher {
    public suspend operator fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse>
}
