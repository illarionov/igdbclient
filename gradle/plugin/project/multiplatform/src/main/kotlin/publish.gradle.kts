/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.multiplatform

import at.released.igdbclient.gradle.multiplatform.publish.createIgdbclientVersionsExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

createIgdbclientVersionsExtension()

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    publishing {
        repositories {
            maven {
                name = "test"
                setUrl(project.rootProject.layout.buildDirectory.dir("localMaven"))
            }
        }
    }

    signAllPublications()

    configure(
        KotlinMultiplatform(javadocJar = JavadocJar.Empty()),
    )

    pom {
        name.set(project.name)
        description.set("Kotlin Multiplatform wrapper for the Internet Game Database API")
        url.set("https://github.com/illarionov/igdbclient")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/igdbclient.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/igdbclient.git")
            url.set("https://github.com/illarionov/igdbclient/tree/main")
        }
    }
}

dokka {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}
