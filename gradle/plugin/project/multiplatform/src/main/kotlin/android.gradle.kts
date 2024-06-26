/*
 * Copyright (c) 2024, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.multiplatform

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getting

/*
 * Convention plugin that configures Android target in projects with the Kotlin Multiplatform plugin
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform") apply false
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
    id("ru.pixnews.igdbclient.gradle.lint.android-lint")
    id("com.android.library")
}

kotlin {
    androidTarget {
        publishLibraryVariants = listOf("release")
    }

    sourceSets {
        val commonMain by getting
        val androidMain by getting
        androidMain.dependsOn(commonMain)

        val commonTest by getting
        val androidUnitTest by getting

        androidUnitTest.dependsOn(commonTest)
    }
}

extensions.configure<LibraryExtension>("android") {
    compileSdk = versionCatalogs.named("libs").findVersion("androidCompileSdk").get().displayName.toInt()
    defaultConfig {
        minSdk = versionCatalogs.named("libs").findVersion("androidMinSdk").get().displayName.toInt()
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = false
    }
}
