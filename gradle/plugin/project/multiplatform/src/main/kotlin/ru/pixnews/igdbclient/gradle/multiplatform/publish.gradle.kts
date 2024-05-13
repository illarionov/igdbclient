/*
 * Copyright (c) 2024, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.multiplatform

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import ru.pixnews.igdbclient.gradle.multiplatform.publish.createIgdbclientVersionsExtension

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform") apply false
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

createIgdbclientVersionsExtension()

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
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
        KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaHtml")),
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

tasks.named<DokkaTask>("dokkaHtml") {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}
