/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient

import at.released.igdbclient.dsl.IgdbHttpEngineConfig
import at.released.igdbclient.internal.IgdbHttpClient
import at.released.igdbclient.internal.model.IgdbClientConfig

public interface IgdbHttpEngine<H : IgdbHttpEngineConfig> {
    /**
     * Creates a new [IgdbHttpClient] specifying a [config].
     */
    public fun create(config: IgdbClientConfig<H>): IgdbHttpClient
}
