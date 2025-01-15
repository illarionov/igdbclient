/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.dsl

import at.released.igdbclient.auth.twitch.TwitchTokenStorage

@IgdbClientDsl
public class TwitchAuthConfig {
    public var enabled: Boolean = true
    public var clientId: String = ""
    public var clientSecret: String = ""
    public var maxRequestRetries: Int = 3
        set(value) {
            check(value > 0) { "maxRequestRetries should be > 0" }
            field = value
        }
    public var storage: TwitchTokenStorage? = null
}
