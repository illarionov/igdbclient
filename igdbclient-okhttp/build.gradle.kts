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
    id("ru.pixnews.igdbclient.gradle.multiplatform.test")
    id("ru.pixnews.igdbclient.gradle.multiplatform.publish")
}

group = "ru.pixnews.igdbclient"
version = igdbVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "igdbclient_okhttp_version",
    envVariableName = "IGDBCLIENT_OKHTTP_VERSION",
).get()

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":igdbclient-core"))
                api(libs.okhttp3)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
            }
        }

        val commonJvmMain by creating
        commonJvmMain.dependsOn(commonMain)

        getByName("androidMain").dependsOn(commonJvmMain)
        getByName("jvmMain").dependsOn(commonJvmMain)

        getByName("jvmTest") {
            dependencies {
                implementation(project(":library:test"))
                implementation(project(":igdbclient-integration-tests"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.kermit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp5.mockwebserver.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

android {
    namespace = "ru.pixnews.igdbclient.okhttp"
}
