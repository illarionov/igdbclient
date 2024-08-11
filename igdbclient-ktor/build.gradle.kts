/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    id("ru.pixnews.igdbclient.gradle.lint.binary.compatibility.validator")
    id("ru.pixnews.igdbclient.gradle.multiplatform.android")
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.multiplatform.publish")
    id("ru.pixnews.igdbclient.gradle.multiplatform.test")
}

group = "ru.pixnews.igdbclient"
version = igdbVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "igdbclient_ktor_version",
    envVariableName = "IGDBCLIENT_KTOR_VERSION",
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

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":igdbclient-core"))
                api(libs.kotlinx.coroutines.core)
                api(libs.ktor.client.core)
                api(libs.okio)
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kermit)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(project(":igdbclient-integration-tests"))
                implementation(project(":library:test"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.kermit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.apache5)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.java)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.logging)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp5.mockwebserver.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

android {
    namespace = "ru.pixnews.igdbclient.ktor"
}
