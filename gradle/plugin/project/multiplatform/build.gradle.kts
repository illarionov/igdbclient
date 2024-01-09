/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.igdbclient.gradle"

dependencies {
    implementation(project(":lint"))
    implementation(libs.agp.plugin.api)
    implementation(libs.atomicfu.plugin)
    implementation(libs.kotlin.plugin)
    implementation(libs.gradle.maven.publish.plugin)
    implementation(libs.dokka.plugin)
    runtimeOnly(libs.agp.plugin)
}
