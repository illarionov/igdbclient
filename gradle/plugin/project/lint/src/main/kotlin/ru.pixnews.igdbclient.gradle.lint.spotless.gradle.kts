/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import ru.pixnews.igdbclient.gradle.lint.configRootDir
import ru.pixnews.igdbclient.gradle.lint.lintedFileTree

/*
 * Convention plugin that configures Spotless
 */
plugins {
    id("com.diffplug.spotless")
}

spotless {
    isEnforceCheck = false

    val rootDir = lintedFileTree

    kotlin {
        target(rootDir.filter { it.name.endsWith(".kt") })

        diktat().configFile(configRootDir.file("diktat.yml"))
        licenseHeaderFile(configRootDir.file("copyright/copyright.kt"))
    }
    kotlinGradle {
        target(rootDir.filter { it.name.endsWith(".gradle.kts") })

        diktat().configFile(configRootDir.file("diktat.yml"))
        licenseHeaderFile(configRootDir.file("copyright/copyright.kt"), "(^(?![\\/ ]\\*).*$)")
    }
    format("properties") {
        // "**/.gitignore" does not work: https://github.com/diffplug/spotless/issues/1146
        val propertyFiles = setOf(
            ".gitignore",
            ".gitattributes",
            ".editorconfig",
            "gradle.properties",
        )
        target(rootDir.filter { it.name in propertyFiles })

        trimTrailingWhitespace()
        endWithNewline()
    }
    format("yaml") {
        target(rootDir.filter { it.name.endsWith(".yml") })

        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(2)
    }
    format("toml") {
        target(rootDir.filter { it.name.endsWith(".toml") })

        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(2)
    }
    format("markdown") {
        target(rootDir.filter { it.name.endsWith(".md") || it.name.endsWith(".markdown") })

        endWithNewline()
    }
    format("protobuf") {
        target(rootDir.filter { it.name.endsWith(".proto") })

        // Disabled until https://github.com/diffplug/spotless/issues/673 is fixed
        // clangFormat("14.0.0-1ubuntu1").style("LLVM")
        endWithNewline()
    }
}
