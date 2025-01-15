/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */
pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("at.released.igdbclient.gradle.settings.root")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../libs.versions.toml"))
        }
    }
}

include("multiplatform")
include("lint")
include("protobuf")

rootProject.name = "gradle-project-plugins"
