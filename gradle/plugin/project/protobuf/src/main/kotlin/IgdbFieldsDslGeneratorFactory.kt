/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.protobuf.igdb

import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Extend
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.SchemaHandler
import com.squareup.wire.schema.Service
import com.squareup.wire.schema.Type
import okio.Path

public class IgdbFieldsDslGeneratorFactory : SchemaHandler.Factory {
    override fun create(
        includes: List<String>,
        excludes: List<String>,
        exclusive: Boolean,
        outDirectory: String,
        options: Map<String, String>,
    ): SchemaHandler = IgdbFieldsDslGenerator()
}

private class IgdbFieldsDslGenerator(
    val fieldClassGenerator: (Type, Context) -> GeneratedFileContent = { type, _ ->
        RequestFieldsDslClassGenerator(type).invoke()
    },
    val enumSchemeClassGenerator: (Type, Context) -> GeneratedFileContent = { type, _ ->
        SchemeEnumClassGenerator(type).invoke()
    },
) : SchemaHandler() {
    override fun handle(extend: Extend, field: Field, context: Context): Path? = null

    override fun handle(service: Service, context: Context): List<Path> = listOf()

    override fun handle(type: Type, context: Context): Path? {
        if ((type is EnumType) ||
            (type.name in IgdbFieldsDslGeneratorPaths.EXCLUDED_MESSAGES) ||
            (type.type.simpleName.endsWith("Result"))
        ) {
            return null
        }
        val generatedSchemeEnumClass = enumSchemeClassGenerator(type, context)
        val generatedFieldsDslClass = fieldClassGenerator(type, context)

        val enumClassPath = writeGeneratedFile(context, generatedSchemeEnumClass)
        val fieldsDslPath = writeGeneratedFile(context, generatedFieldsDslClass)

        context.claimedPaths.claim(enumClassPath, type)

        return fieldsDslPath
    }

    private fun writeGeneratedFile(
        context: Context,
        generatedFile: GeneratedFileContent,
    ): Path {
        val outDirectory = context.outDirectory
        val fileSystem = context.fileSystem
        val path = outDirectory / generatedFile.filePath.joinToString(separator = "/")
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) { writeUtf8(generatedFile.content) }
        return path
    }
}
