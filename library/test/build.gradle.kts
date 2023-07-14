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
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("ru.pixnews.igdbclient.gradle.lint.android-lint")
}

group = "ru.pixnews.igdbclient.library.test"

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    jvmToolchain(17)
    explicitApi = ExplicitApiMode.Warning

    targetHierarchy.default()

    jvm()
    android()
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
        all {
            languageSettings {
                languageVersion = "1.8"
                apiVersion = "1.8"
                listOf(
                    "kotlin.RequiresOptIn",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                ).forEach(::optIn)
            }
        }

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
                implementation(libs.okhttp3.mockwebserver)
            }
        }
        getByName("jvmMain") {
            dependencies {
                api(libs.junit.jupiter.api)
                implementation(libs.okhttp3)
                implementation(libs.okhttp3.mockwebserver)
                implementation(libs.okhttp3.logging.interceptor)
            }
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            // https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces/
            "-Xjvm-default=all",
        )
    }
}

android {
    namespace = "ru.pixnews.igdbclient"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = false
    }
}
