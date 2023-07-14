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
package ru.pixnews.igdbclient.library.test

public object IgdbClientConstants {
    public const val TWITCH_AUTH_URL: String = "https://id.twitch.tv/oauth2/token"

    public object MediaType {
        public const val APPLICATION_PROTOBUF: String = "application/protobuf"
        public const val APPLICATION_JSON: String = "application/json"
        public const val TEXT_PLAIN: String = "text/plain"
    }

    public object Header {
        public const val CLIENT_ID: String = "Client-ID"
        public const val AUTHORIZATION: String = "Authorization"
    }
}
