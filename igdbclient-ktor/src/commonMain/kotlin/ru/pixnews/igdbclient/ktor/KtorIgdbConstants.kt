/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.ktor

import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder

internal object KtorIgdbConstants {
    const val DEFAULT_BUFFER_SIZE: Long = 64 * 1024L
    val TWITCH_AUTH_URL = URLBuilder("https://id.twitch.tv/oauth2/token").build()
    object Header {
        const val CLIENT_ID = "Client-ID"
        val AUTHORIZATION = HttpHeaders.Authorization
    }
}
