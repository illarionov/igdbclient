/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.model.IgdbWebhook
import okio.BufferedSource

@InternalIgdbClientApi
public object IgdbParser

/**
 * Igdb server response parser
 *
 * Parse incoming [source] response from the Igdb server into a [IgdbHttpErrorResponse] object.
 *
 * Note: It is the caller's responsibility to close this stream.
 *
 * @return [IgdbHttpErrorResponse] or null if the stream cannot be parsed
 */
@InternalIgdbClientApi
public expect fun IgdbParser.igdbErrorResponseParser(source: BufferedSource): IgdbHttpErrorResponse

@InternalIgdbClientApi
internal expect fun IgdbParser.igdbWebhookListJsonParser(source: BufferedSource): List<IgdbWebhook>
