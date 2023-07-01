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

import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("ru.pixnews.igdbclient.gradle.kotlin.atomicfu")
    id("ru.pixnews.igdbclient.gradle.lint.android-lint")
    id("ru.pixnews.igdbclient.gradle.protobuf-wire")
    `maven-publish`
}

group = "ru.pixnews.igdbclient"
version = "0.1"

kotlin {
    jvmToolchain(17)
    explicitApi = ExplicitApiMode.Warning

    jvm()
    android()
    linuxX64()

    sourceSets {
        all {
            languageSettings {
                languageVersion = "1.8"
                apiVersion = "1.8"
                listOf(
                    "kotlin.RequiresOptIn",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "ru.pixnews.igdbclient.InternalIgdbClientApi",
                ).forEach(::optIn)
            }
        }
        /* Main source sets */
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
            }
        }
        val nativeMain by creating
        val jvmMain by getting {
            dependencies {
                implementation(libs.org.json)
            }
        }
        val androidMain by getting
        val linuxMain by creating
        val linuxX64Main by getting

        /* Main hierarchy */
        nativeMain.dependsOn(commonMain)
        jvmMain.dependsOn(commonMain)
        androidMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        linuxX64Main.dependsOn(linuxMain)

        /* Test source sets */
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val nativeTest by creating
        val jvmTest by getting
        val androidUnitTest by getting
        val linuxTest by creating
        val linuxX64Test by getting

        /* Test hierarchy */
        nativeTest.dependsOn(commonTest)
        jvmTest.dependsOn(commonTest)
        androidUnitTest.dependsOn(commonTest)
        linuxTest.dependsOn(nativeTest)
        linuxX64Test.dependsOn(linuxTest)
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
