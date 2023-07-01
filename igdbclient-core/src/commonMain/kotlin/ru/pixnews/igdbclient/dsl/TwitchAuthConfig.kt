/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.dsl

import ru.pixnews.igdbclient.auth.twitch.TwitchTokenStorage

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
