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

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.kotlin.dsl.apply
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

tasks.named<DokkaTask>("dokkaHtml") {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}
