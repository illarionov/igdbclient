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

import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
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
        val androidMain by getting
        val iosArm64Main by getting
        val iosMain by creating
        val iosSimulatorArm64Main by getting
        val iosX64Main by getting
        val jsMain by getting
        val jvmMain by getting {
            dependencies {
                implementation(libs.org.json)
            }
        }
        val linuxMain by creating
        val linuxX64Main by getting
        val macosArm64Main by getting
        val macosMain by creating
        val macosX64Main by getting
        val mingwX64Main by getting
        val nativeMain by creating {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val windowsMain by creating

        /* Main hierarchy */
        commonMain.hasChildren(
            androidMain,
            jsMain,
            jvmMain,
            nativeMain.hasChildren(
                iosMain.hasChildren(
                    iosX64Main,
                    iosArm64Main,
                    iosSimulatorArm64Main,
                ),
                linuxMain.hasChildren(
                    linuxX64Main,
                ),
                macosMain.hasChildren(
                    macosX64Main,
                    macosArm64Main,
                ),
                windowsMain.hasChildren(
                    mingwX64Main,
                ),
            ),
        )

        /* Test source sets */
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kermit)
            }
        }
        val nativeTest by creating
        val jvmTest by getting {
            dependencies {
                implementation(project(":library:test"))
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotest.assertions.core)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kermit.jvm)
            }
        }
        val jsTest by getting
        val iosTest by creating
        val linuxTest by creating
        val macosTest by creating
        val windowsTest by creating
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val linuxX64Test by getting
        val macosX64Test by getting
        val macosArm64Test by getting
        val mingwX64Test by getting

        /* Test hierarchy */
        commonTest.hasChildren(
            nativeTest.hasChildren(
                iosTest.hasChildren(
                    iosX64Test,
                    iosArm64Test,
                    iosSimulatorArm64Test,
                ),
                linuxTest.hasChildren(
                    linuxX64Test,
                ),
                macosTest.hasChildren(
                    macosX64Test,
                    macosArm64Test,
                ),
                windowsTest.hasChildren(
                    mingwX64Test,
                ),
            ),
            jvmTest,
            androidUnitTest,
            jsTest,
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "2G"
    testLogging {
        events = mutableSetOf(
            FAILED,
        )
    }
}

fun KotlinSourceSet.hasChildren(vararg childSourceSets: KotlinSourceSet): KotlinSourceSet {
    childSourceSets.forEach {
        it.dependsOn(this)
    }
    return this
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
