/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EMPTY_PRIMARY_CONSTRUCTOR")

package at.released.igdbclient.library.test

import co.touchlab.kermit.LogWriter

internal expect val testLogWriter: LogWriter

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreAndroid()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreJs()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreJvm()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreNative()
