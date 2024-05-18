/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
