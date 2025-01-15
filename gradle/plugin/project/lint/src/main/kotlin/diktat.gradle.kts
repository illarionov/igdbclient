/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.lint

import com.saveourtool.diktat.plugin.gradle.tasks.DiktatTaskBase
import org.gradle.kotlin.dsl.withType

/*
 * Convention plugin that configures Diktat
 */
plugins {
    id("com.saveourtool.diktat")
}

diktat {
    diktatConfigFile = configRootDir.file("diktat.yml").asFile
    inputs {
        include("**/*.kt")
        include("**/*.gradle.kts")
        excludeNonLintedDirectories()
    }
    reporters {
        plain()
        sarif()
    }
    debug = false
}

tasks.named("mergeDiktatReports").configure {
    enabled = false
}

tasks.withType<DiktatTaskBase>().configureEach {
    notCompatibleWithConfigurationCache("invocation of 'Task.project' at execution time is unsupported")
}
