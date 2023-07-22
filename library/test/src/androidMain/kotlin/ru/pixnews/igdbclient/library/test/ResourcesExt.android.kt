/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.library.test

import okio.Buffer
import okio.source

private object AndroidResourcesClass

internal actual fun readResourceAsBuffer(path: String): Buffer {
    return Buffer().apply {
        val stream = AndroidResourcesClass.javaClass.getResourceAsStream(path) ?: error("No resource `$path`")
        stream.source().use {
            writeAll(it)
        }
    }
}
