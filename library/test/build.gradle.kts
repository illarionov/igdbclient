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
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
}

group = "ru.pixnews.igdbclient.library.test"

kotlin {
    jvmToolchain(17)
    explicitApi = ExplicitApiMode.Warning
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        languageVersion.set(KOTLIN_1_8)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
        if (this@configureEach.name.endsWith("TestFixturesKotlin")) {
            freeCompilerArgs.addAll("-Xexplicit-api=warning")
        }
    }
}

dependencies {
    api(libs.junit.jupiter.api)
    api(libs.kotlinx.coroutines.test)
    api(libs.kermit)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.mockwebserver)
    implementation(libs.okhttp3.logging.interceptor)
}
