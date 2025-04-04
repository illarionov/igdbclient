/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

internal actual val testLogWriter: LogWriter = platformLogWriter()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreAndroid

public actual typealias IgnoreJs = kotlin.test.Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreJvm

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreNative
