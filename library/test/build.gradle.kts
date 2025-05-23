/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("at.released.igdbclient.gradle.multiplatform.kotlin")
    id("at.released.igdbclient.gradle.multiplatform.android")
}

group = "at.released.igdbclient.library.test"

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(kotlin("test"))
                api(libs.assertk)
                api(libs.kermit)
                api(libs.kotlinx.coroutines.test)
                api(libs.okio)
            }
        }
        getByName("androidMain") {
            dependencies {
                api(libs.junit.jupiter.api)
                implementation(libs.okhttp3)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp5.mockwebserver.junit5)
            }
        }
        getByName("jvmMain") {
            dependencies {
                api(libs.junit.jupiter.api)
                implementation(libs.okhttp3)
                implementation(libs.okhttp5.mockwebserver.junit5)
                implementation(libs.okhttp3.logging.interceptor)
            }
        }
    }
}

android {
    namespace = "at.released.igdbclient.library.test"
    packaging {
        resources.excludes += listOf(
            "META-INF/LICENSE-notice.md",
            "META-INF/LICENSE.md",
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }
}
