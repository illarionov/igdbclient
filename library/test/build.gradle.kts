/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.multiplatform.android")
}

group = "ru.pixnews.igdbclient.library.test"

kotlin {
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }
}
