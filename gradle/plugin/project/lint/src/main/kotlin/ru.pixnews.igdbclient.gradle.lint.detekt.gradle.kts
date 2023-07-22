/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import io.gitlab.arturbosch.detekt.Detekt
import ru.pixnews.igdbclient.gradle.base.versionCatalog
import ru.pixnews.igdbclient.gradle.lint.configRootDir
import ru.pixnews.igdbclient.gradle.lint.lintedFileTree

/*
 * Convention plugin that creates and configures task designated to run Detekt static code analyzer
 */
plugins {
    id("io.gitlab.arturbosch.detekt")
}

val detektCheck = tasks.register("detektCheck", Detekt::class) {
    description = "Custom detekt for to check all modules"

    this.config.setFrom(configRootDir.file("detekt.yml"))
    setSource(
        lintedFileTree
            .matching {
                exclude("**/resources/**")
            }
            .filter {
                it.name.endsWith(".kt") || it.name.endsWith(".kts")
            },
    )
    basePath = rootProject.projectDir.toString()

    parallel = true
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = true

    reports {
        html.required.set(true)
        md.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)

        xml.outputLocation.set(file("build/reports/detekt/report.xml"))
        html.outputLocation.set(file("build/reports/detekt/report.html"))
        txt.outputLocation.set(file("build/reports/detekt/report.txt"))
        sarif.outputLocation.set(file("build/reports/detekt/report.sarif"))
    }
}

// https://github.com/gradle/gradle/issues/22468
if (project.name != "gradle-kotlin-dsl-accessors") {
    dependencies {
        detektPlugins(versionCatalog.findLibrary("detekt.formatting").get())
    }
}
