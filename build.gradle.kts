/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.gradle.maven.publish.plugin.base) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    id("at.released.igdbclient.gradle.lint.detekt")
    id("at.released.igdbclient.gradle.lint.diktat")
    id("at.released.igdbclient.gradle.lint.spotless")
}

group = "at.releaed.igdbclient"

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests and Android Lint)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
