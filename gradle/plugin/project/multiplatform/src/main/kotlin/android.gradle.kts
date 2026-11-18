/*
 * Copyright (c) 2024-2026, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.multiplatform

import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

/*
 * Convention plugin that configures Android library target in projects with the Kotlin Multiplatform plugin
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("at.released.igdbclient.gradle.multiplatform.kotlin")
    id("at.released.igdbclient.gradle.lint.android-lint")
}

kotlin {
    android {
        minSdk = versionCatalogs.named("libs").findVersion("androidMinSdk").get().displayName.toInt()
        compileSdk = versionCatalogs.named("libs").findVersion("androidCompileSdk").get().displayName.toInt()
        aarMetadata.minCompileSdk = 1
        enableCoreLibraryDesugaring = false

        compilerOptions {
            jvmTarget.set(JVM_17)
        }
    }
}
