/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    @Suppress("OPT_IN_USAGE")
    targetHierarchy.default()

    android()
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
