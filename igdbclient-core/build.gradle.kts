/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    id("ru.pixnews.igdbclient.gradle.lint.binary.compatibility.validator")
    id("ru.pixnews.igdbclient.gradle.multiplatform.android")
    id("ru.pixnews.igdbclient.gradle.multiplatform.atomicfu")
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.multiplatform.publish")
    id("ru.pixnews.igdbclient.gradle.multiplatform.test")
    id("ru.pixnews.igdbclient.gradle.protobuf.igdb.wire")
}

group = "ru.pixnews.igdbclient"
version = igdbVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "igdbclient_core_version",
    envVariableName = "IGDBCLIENT_CORE_VERSION",
).get()

kotlin {
    applyDefaultHierarchyTemplate()

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
    namespace = "ru.pixnews.igdbclient"
}
