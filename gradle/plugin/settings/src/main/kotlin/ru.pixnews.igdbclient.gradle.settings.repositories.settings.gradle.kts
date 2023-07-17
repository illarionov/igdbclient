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

/*
 * Settings convention plugin that configures repositories used in the application
 */
pluginManagement {
    repositories {
        googleExclusiveContent()
        gradlePluginPortal()
    }

    // Get our own convention plugins from 'gradle/plugin/project'
    listOf(
        "project" to "gradle-project-plugins",
    ).forEach { (path, gradleProjectsPluginName) ->
        if (File(rootDir, "gradle/plugin/$path").exists()) {
            includeBuild("gradle/plugin/$path") {
                name = gradleProjectsPluginName
            }
        }
        // If not the main build, 'project' is located next to the build
        if (File(rootDir, "../$path").exists()) {
            includeBuild("../$path") {
                name = gradleProjectsPluginName
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        googleExclusiveContent()
        mavenCentral()
    }
}

fun RepositoryHandler.googleExclusiveContent(): Unit = exclusiveContent {
    forRepository(::google)
    filter {
        // https://maven.google.com/web/index.html
        includeGroupByRegex("""android\.arch\..*""")
        includeGroupByRegex("""androidx\..+""")
        includeGroupByRegex("""com\.android(?:\..+)?""")
        includeGroupByRegex("""com\.crashlytics\.sdk\.android\..*""")
        includeGroupByRegex("""com\.google\.ads\..*""")
        includeGroupByRegex("""com\.google\.android\..*""")
        listOf(
            "com.google.ambient.crossdevice",
            "com.google.androidbrowserhelper",
            "com.google.ar",
            "com.google.ar.sceneform",
            "com.google.ar.sceneform.ux",
            "com.google.assistant.appactions",
            "com.google.assistant.suggestion",
            "com.google.camerax.effects",
            "com.google.chromeos",
            "com.google.d2c",
            "com.google.fhir",
            "com.google.firebase",
            "com.google.firebase.appdistribution",
            "com.google.firebase.crashlytics",
            "com.google.firebase.firebase-perf",
            "com.google.firebase.testlab",
            "com.google.gms",
            "com.google.gms.google-services",
            "com.google.jacquard",
            "com.google.mediapipe",
            "com.google.mlkit",
            "com.google.net.cronet",
            "com.google.oboe",
            "com.google.prefab",
            "com.google.relay",
            "com.google.test.platform",
            "com.google.testing.platform",
            "io.fabric.sdk.android",
            "tools.base.build-system.debug",
            "zipflinger",
        ).map(::includeGroup)

        includeModuleByRegex(
            """org\.jetbrains\.kotlin""",
            """kotlin-ksp|kotlin-symbol-processing-api""",
        )
    }
}
