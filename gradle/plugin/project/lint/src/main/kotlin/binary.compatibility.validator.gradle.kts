/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.lint

import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/*
 * Convention plugin that configures Kotlin's built-in ABI validation
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

@OptIn(ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters.exclude.annotatedWith.add("at.released.igdbclient.InternalIgdbClientApi")
    }
}
