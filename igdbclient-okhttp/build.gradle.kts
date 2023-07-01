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
    id("ru.pixnews.igdbclient.gradle.lint.android-lint")
    `maven-publish`
}

/* required for maven publication */
group = "ru.pixnews.igdbclient"
version = "0.1"

kotlin {
    jvmToolchain(17)
    explicitApi = ExplicitApiMode.Warning

    jvm()
    android()

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
        val commonJvmMain by creating {
            dependencies {
                implementation(project(":igdbclient-core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
                implementation(libs.okhttp3)
            }
        }
        val jvmMain by getting
        val androidMain by getting
        /* Main hierarchy */
        jvmMain.dependsOn(commonJvmMain)
        androidMain.dependsOn(commonJvmMain)

        /* Test source sets */
        val commonJvmTest by creating
        val jvmTest by getting
        val androidUnitTest by getting

        /* Test hierarchy */
        jvmTest.dependsOn(commonJvmTest)
        androidUnitTest.dependsOn(commonJvmTest)
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
    namespace = "ru.pixnews.igdbclient.okhttp"
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
