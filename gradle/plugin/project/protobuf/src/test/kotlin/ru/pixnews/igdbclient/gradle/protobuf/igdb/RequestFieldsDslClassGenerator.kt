/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.protobuf.igdb

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
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.DEFAULT_FIELD
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.DEFAULT_MESSAGE_TYPE
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_AGE_RATING_MODEL_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_CLIENT_DSL_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_FIELD_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_GAME_MODEL_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_REQUEST_FIELD_DSL_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.IGDB_REQUEST_FIELD_STUB
import ru.pixnews.igdbclient.gradle.protobuf.igdb.FieldsTestFixtures.forceSetType

@OptIn(ExperimentalCompilerApi::class)
class RequestFieldsDslClassGenerator {
    @Test
    fun `generated field class should compile`() {
        val gameType = DEFAULT_MESSAGE_TYPE.copy(
            type = ProtoType.get("ru.pixnews.igdbclient.model.Game"),
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
                ).forceSetType(ProtoType.get("ru.pixnews.igdbclient.model.AgeRating")),
            ),
        )

        val ageRatingType = DEFAULT_MESSAGE_TYPE.copy(
            type = ProtoType.get("ru.pixnews.igdbclient.model.AgeRating"),
            name = "AgeRating",
            declaredFields = listOf(
                DEFAULT_FIELD.copy(
                    name = "id",
                    namespaces = listOf("ru.pixnews.igdbclient.model", "AgeRating"),
                    tag = 1,
                    elementType = "uint64",
                ).forceSetType(ProtoType.UINT64),
            ),
        )

        val compilationResult = compileFieldsClass(gameType, ageRatingType)
        compilationResult.exitCode shouldBe OK
    }

    private fun compileFieldsClass(vararg types: Type): JvmCompilationResult {
        val generatedSources = types
            .map { type ->
                val generatedSourceText = SchemeEnumClassGenerator(type).invoke()

                SourceFile.kotlin(
                    generatedSourceText.filePath.last(),
                    generatedSourceText.content,
                )
            }

        return KotlinCompilation().apply {
            sources = listOf(
                IGDB_AGE_RATING_MODEL_STUB,
                IGDB_CLIENT_DSL_STUB,
                IGDB_FIELD_STUB,
                IGDB_GAME_MODEL_STUB,
                IGDB_REQUEST_FIELD_STUB,
                IGDB_REQUEST_FIELD_DSL_STUB,
            ) + generatedSources
            inheritClassPath = false
            messageOutputStream = System.out
        }.compile()
    }
}
