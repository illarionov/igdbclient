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
    id("ru.pixnews.igdbclient.gradle.lint.binary.compatibility.validator")
    id("ru.pixnews.igdbclient.gradle.multiplatform.android")
    id("ru.pixnews.igdbclient.gradle.multiplatform.atomicfu")
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.multiplatform.publish")
    id("ru.pixnews.igdbclient.gradle.multiplatform.test")
    id("ru.pixnews.igdbclient.gradle.protobuf-wire")
}

group = "ru.pixnews.igdbclient"
version = "0.1"

kotlin {
    @Suppress("OPT_IN_USAGE")
    targetHierarchy.default()

    android()
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
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

android {
    namespace = "ru.pixnews.igdbclient"
}
