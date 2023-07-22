/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.ktor

import ru.pixnews.igdbclient.IgdbHttpEngine
import ru.pixnews.igdbclient.internal.IgdbHttpClient
import ru.pixnews.igdbclient.internal.model.IgdbClientConfig

/**
 * [IgdbHttpEngine] that uses [io.ktor.client.HttpClient] to execute network requests
 */
public object IgdbKtorEngine : IgdbHttpEngine<IgdbKtorConfig> {
    override fun create(config: IgdbClientConfig<IgdbKtorConfig>): IgdbHttpClient =
        KtorIgdbHttpClientImplementation(config)
}
