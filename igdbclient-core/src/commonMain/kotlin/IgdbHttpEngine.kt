/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient

import ru.pixnews.igdbclient.dsl.IgdbHttpEngineConfig
import ru.pixnews.igdbclient.internal.IgdbHttpClient
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig

public interface IgdbHttpEngine<H : IgdbHttpEngineConfig> {
    /**
     * Creates a new [IgdbHttpClient] specifying a [config].
     */
    public fun create(config: IgdbClientConfig<H>): IgdbHttpClient
}
