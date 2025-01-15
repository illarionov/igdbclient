/*
 * Copyright (c) 2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import org.json.JSONParserConfiguration
import org.json.JSONTokener

private val parserConfiguration = JSONParserConfiguration()
    .withStrictMode()

internal fun String.jsonTokener(): JSONTokener = JSONTokener(this).apply {
    this.jsonParserConfiguration = parserConfiguration
}
