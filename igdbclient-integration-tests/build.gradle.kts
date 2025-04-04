/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("at.released.igdbclient.gradle.multiplatform.kotlin")
}

group = "at.released.igdbclient"
version = "0.1"

kotlin {
    explicitApi = null

    jvm()

    sourceSets {
        /* Main source sets */
        named("commonMain") {
            dependencies {
                api(project(":igdbclient-core"))
                implementation(libs.kermit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.core)
                implementation(libs.okio)
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(project(":library:test"))
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp5.mockwebserver.junit5)
                implementation(libs.slf4j.simple)
            }
        }
    }
}
