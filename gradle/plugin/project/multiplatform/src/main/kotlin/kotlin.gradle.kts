/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.multiplatform

import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/*
 * Convention plugin that configures Kotlin in projects with the Kotlin Multiplatform plugin
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    explicitApi = ExplicitApiMode.Warning

    sourceSets {
        all {
            languageSettings {
                languageVersion = "1.9"
                apiVersion = "1.9"
                listOf(
                    "kotlin.RequiresOptIn",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "at.released.igdbclient.InternalIgdbClientApi",
                ).forEach(::optIn)
            }
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
        )
    }
}
