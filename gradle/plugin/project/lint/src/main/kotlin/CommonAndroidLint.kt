/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.lint

import com.android.build.api.dsl.Lint

/**
 * Android Lint defaults
 */
internal fun Lint.configureCommonAndroidLint() {
    quiet = true
    ignoreWarnings = false
    htmlReport = true
    xmlReport = true
    sarifReport = true
    checkDependencies = false
    ignoreTestSources = true

    disable += "ObsoleteSdkInt"
    informational += "GradleDependency"
}
