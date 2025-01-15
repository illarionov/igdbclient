/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.twitch

import at.released.igdbclient.IgdbResult
import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.TwitchToken

@InternalIgdbClientApi
public interface TwitchTokenFetcher {
    public suspend operator fun invoke(credentials: TwitchCredentials): IgdbResult<TwitchToken, TwitchErrorResponse>
}
