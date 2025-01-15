/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.SystemWriter

internal actual val testLogWriter: LogWriter = SystemWriter()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreAndroid

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreJs

public actual typealias IgnoreJvm = org.junit.jupiter.api.Disabled

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreNative
