/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    id("at.released.igdbclient.gradle.lint.binary.compatibility.validator")
    id("at.released.igdbclient.gradle.multiplatform.android")
    id("at.released.igdbclient.gradle.multiplatform.atomicfu")
    id("at.released.igdbclient.gradle.multiplatform.kotlin")
    id("at.released.igdbclient.gradle.multiplatform.publish")
    id("at.released.igdbclient.gradle.multiplatform.test")
    id("at.released.igdbclient.gradle.protobuf.igdb.wire")
}

group = "at.released.igdbclient"
version = igdbVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "igdbclient_core_version",
    envVariableName = "IGDBCLIENT_CORE_VERSION",
).get()

kotlin {
    androidTarget()
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

    applyDefaultHierarchyTemplate()

    sourceSets {
        /* Main source sets */
        getByName("commonMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(libs.org.json)
            }
        }
        getByName("nativeMain") {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }

        /* Test source sets */
        getByName("commonTest") {
            dependencies {
                implementation(project(":library:test"))
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kermit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotest.assertions.core)
                runtimeOnly(libs.junit.jupiter.engine)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

android {
    namespace = "at.released.igdbclient"
}
