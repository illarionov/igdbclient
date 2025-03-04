/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.protobuf.igdb

import at.released.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.DEFAULT_FIELD
import at.released.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.DEFAULT_MESSAGE_TYPE
import at.released.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.forceSetType
import com.squareup.wire.schema.Field.Label
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Type
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class SchemeEnumClassGeneratorTest {
    @Test
    fun `generated field class should compile`() {
        val gameType = DEFAULT_MESSAGE_TYPE.copy(
            type = ProtoType.get("at.released.igdbclient.model.Game"),
            name = "Game",
            declaredFields = listOf(
                DEFAULT_FIELD.copy(
                    name = "id",
                    tag = 1,
                    elementType = "uint64",
                ).forceSetType(ProtoType.UINT64),
                DEFAULT_FIELD.copy(
                    name = "parent",
                    tag = 1,
                    elementType = "AgeRating",
                    label = Label.REPEATED,
                ).forceSetType(ProtoType.get("at.released.igdbclient.model.AgeRating")),
            ),
        )

        val compilationResult = compileEnumFieldsClass(gameType)
        compilationResult.exitCode shouldBe OK
    }

    private fun compileEnumFieldsClass(type: Type): JvmCompilationResult {
        val generatedSourceText = SchemeEnumClassGenerator(type).invoke()
        val generatedSource = SourceFile.kotlin(
            name = generatedSourceText.filePath.last(),
            contents = generatedSourceText.content,
        )

        return KotlinCompilation().apply {
            sources = listOf(
                FieldsTestFixtures.IGDB_CLIENT_DSL_STUB,
                FieldsTestFixtures.IGDB_FIELD_STUB,
                FieldsTestFixtures.IGDB_GAME_MODEL_STUB,
                generatedSource,
            )
            inheritClassPath = false
            messageOutputStream = System.out
        }.compile()
    }
}
