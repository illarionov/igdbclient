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

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.multiplatform.android")
}

group = "ru.pixnews.igdbclient.library.test"

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

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
                implementation(libs.kermit.jvm)
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
    namespace = "ru.pixnews.igdbclient.library.test"
}
