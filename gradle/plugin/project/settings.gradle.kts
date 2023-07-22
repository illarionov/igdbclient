/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("ru.pixnews.igdbclient.gradle.settings.root")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../libs.versions.toml"))
        }
    }
}

include("utils")
include("multiplatform")
include("lint")
include("protobuf")

rootProject.name = "gradle-project-plugins"
