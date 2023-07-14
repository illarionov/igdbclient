/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.library.test

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
