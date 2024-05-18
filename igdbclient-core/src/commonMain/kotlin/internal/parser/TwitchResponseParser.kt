/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.internal.model.TwitchToken
import ru.pixnews.igdbclient.internal.twitch.TwitchErrorResponse

/**
 * Parser of the response received from Twitch server.
 */
@InternalIgdbClientApi
public expect fun IgdbParser.twitchTokenErrorResponseParser(source: BufferedSource): TwitchErrorResponse

/**
 * Parser for JSON response with token received from the Twitch server during the Client Credentials
 * Grant Flow.
 */
@InternalIgdbClientApi
public expect fun IgdbParser.twitchTokenParser(source: BufferedSource): TwitchToken
