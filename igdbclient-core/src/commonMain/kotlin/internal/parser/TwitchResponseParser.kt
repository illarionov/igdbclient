/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.internal.model.TwitchToken
import at.released.igdbclient.internal.twitch.TwitchErrorResponse
import okio.BufferedSource

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
