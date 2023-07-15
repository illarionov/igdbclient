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

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("ru.pixnews.igdbclient.gradle.multiplatform.kotlin")
}

group = "ru.pixnews.igdbclient"
version = "0.1"

kotlin {
    explicitApi = null

    jvm()

    sourceSets {
        /* Main source sets */
        val commonMain by getting {
            dependencies {
                api(project(":igdbclient-core"))
                implementation(libs.kermit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.core)
                implementation(libs.okio)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":library:test"))
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.okhttp5.mockwebserver.junit5)
                implementation(libs.slf4j.simple)
            }
        }

        /* Main hierarchy */
        jvmMain.dependsOn(commonMain)
    }
}
