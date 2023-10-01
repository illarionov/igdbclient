/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.igdbclient.gradle.protobuf"

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1G"
    testLogging {
        events = setOf(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.wire.plugin)
    api(libs.wire.schema)
    implementation(libs.kotlinpoet) {
        exclude(module = "kotlin-reflect")
    }

    testImplementation(platform(libs.kotest.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(kotlin("scripting-compiler"))
}
