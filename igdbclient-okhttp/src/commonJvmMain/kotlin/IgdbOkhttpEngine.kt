/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.okhttp

import at.released.igdbclient.IgdbHttpEngine
import at.released.igdbclient.internal.IgdbHttpClient
import at.released.igdbclient.internal.model.IgdbClientConfig
import at.released.igdbclient.okhttp.dsl.IgdbOkhttpConfig

/**
 * [IgdbHttpEngine] that uses [okhttp3.OkHttpClient] to execute network requests
 */
public object IgdbOkhttpEngine : IgdbHttpEngine<IgdbOkhttpConfig> {
    override fun create(config: IgdbClientConfig<IgdbOkhttpConfig>): IgdbHttpClient =
        OkhttpIgdbHttpClientImplementation(config)
}
