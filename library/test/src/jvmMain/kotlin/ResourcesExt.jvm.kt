/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test

import okio.Buffer
import okio.source

private object ResourcesClass

internal actual fun readResourceAsBuffer(path: String): Buffer {
    return Buffer().apply {
        val stream = ResourcesClass.javaClass.getResourceAsStream(path) ?: error("No resource `$path`")
        stream.source().use {
            writeAll(it)
        }
    }
}
