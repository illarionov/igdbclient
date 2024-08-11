/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.library.test

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

internal actual val testLogWriter: LogWriter = platformLogWriter()

public actual typealias IgnoreAndroid = org.junit.jupiter.api.Disabled

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreJs

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreJvm

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class IgnoreNative
