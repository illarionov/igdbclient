/*
 * Copyright (c) 2024, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.multiplatform

import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that configures kotlinx-atomicfu in projects with the Kotlin Multiplatform plugin
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = "kotlinx-atomicfu")

    the<KotlinMultiplatformExtension>().sourceSets {
        getByName("commonTest") {
            dependencies {
                implementation(versionCatalogs.named("libs").findLibrary("kotlinx-atomicfu").orElseThrow())
            }
        }
    }
}
