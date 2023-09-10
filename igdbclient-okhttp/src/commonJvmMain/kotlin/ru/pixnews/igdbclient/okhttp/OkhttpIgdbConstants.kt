/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.okhttp

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType

internal object OkhttpIgdbConstants {
    val TWITCH_AUTH_URL = "https://id.twitch.tv/oauth2/token".toHttpUrl()

    object MediaType {
        const val APPLICATION_PROTOBUF = "application/protobuf"
        const val APPLICATION_JSON = "application/json"
        val TEXT_PLAIN = "text/plain".toMediaType()
    }

    object Header {
        const val CLIENT_ID = "Client-ID"
        const val AUTHORIZATION = "Authorization"
    }
}
