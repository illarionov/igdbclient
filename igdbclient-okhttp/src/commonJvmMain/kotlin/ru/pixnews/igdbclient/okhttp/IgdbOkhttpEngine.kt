/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.okhttp

import ru.pixnews.igdbclient.IgdbHttpEngine
import ru.pixnews.igdbclient.internal.IgdbHttpClient
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig
import ru.pixnews.igdbclient.okhttp.dsl.IgdbOkhttpConfig

/**
 * [IgdbHttpEngine] that uses [okhttp3.OkHttpClient] to execute network requests
 */
public object IgdbOkhttpEngine : IgdbHttpEngine<IgdbOkhttpConfig> {
    override fun create(config: IgdbClientConfig<IgdbOkhttpConfig>): IgdbHttpClient =
        OkhttpIgdbHttpClientImplementation(config)
}
