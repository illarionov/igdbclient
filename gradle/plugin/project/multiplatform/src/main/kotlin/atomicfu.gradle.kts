/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.multiplatform

/*
 * Convention plugin that configures kotlinx-atomicfu in projects with the Kotlin Multiplatform plugin
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = "org.jetbrains.kotlinx.atomicfu")
}
