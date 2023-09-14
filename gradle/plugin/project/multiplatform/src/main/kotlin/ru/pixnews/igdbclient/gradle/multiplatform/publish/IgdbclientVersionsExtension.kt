/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.multiplatform.publish

import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import java.io.StringReader
import java.util.Properties
import javax.inject.Inject

private const val VERSION_PROPERTIES_PATH = "config/version.properties"

internal fun Project.createIgdbclientVersionsExtension(): IgdbclientVersionsExtension {
    val rootProjectDirectory = project.rootProject.layout.projectDirectory
    return extensions.create<IgdbclientVersionsExtension>("igdbVersions").apply {
        this.propertiesFile.convention(
            rootProjectDirectory.file(VERSION_PROPERTIES_PATH),
        )
    }
}

open class IgdbclientVersionsExtension
@Inject
constructor(
    objects: ObjectFactory,
    private val providers: ProviderFactory,
) {
    internal val propertiesFile: RegularFileProperty = objects.fileProperty()
    private val propertiesProvider: Provider<Properties> = providers.fileContents(propertiesFile)
        .asText
        .map(
            object : Transformer<Properties, String> {
                var properties: Properties? = null
                override fun transform(text: String): Properties {
                    return properties ?: run {
                        Properties().apply {
                            load(StringReader(text))
                        }.also { properties = it }
                    }
                }
            },
        )
        .orElse(providers.provider { error("File ${propertiesFile.get()} not found") })
    private val rootVersion: Provider<String> = providers.gradleProperty("version")
        .orElse(providers.environmentVariable("IGDBCLIENT_VERSION"))
        .orElse(
            propertiesProvider.map { props ->
                props.getProperty("igdbclient_version")
                    ?: error("No `igdbclient_version` in ${propertiesFile.get()}")
            },
        )

    fun getSubmoduleVersionProvider(
        propertiesFileKey: String,
        envVariableName: String,
        gradleKey: String = propertiesFileKey,
    ): Provider<String> = providers.gradleProperty(gradleKey)
        .orElse(providers.environmentVariable(envVariableName))
        .orElse(
            propertiesProvider.map { props ->
                props.getProperty(propertiesFileKey)
            },
        )
        .orElse(rootVersion)
}
