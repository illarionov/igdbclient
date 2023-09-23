/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.protobuf.igdb

import com.squareup.wire.Syntax.PROTO_3
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.ProtoType
import com.tschuchort.compiletesting.SourceFile
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

internal object FieldsTestFixtures {
    val DEFAULT_MESSAGE_TYPE = MessageType(
        type = ProtoType.get("ru.pixnews.igdbclient.model.Game"),
        location = Location.get("/datasource-igdb/src/main/proto/igdbapi.proto:469:1"),
        documentation = "Test documentation",
        name = "Game",
        declaredFields = listOf(),
        extensionFields = mutableListOf(),
        oneOfs = listOf(),
        nestedTypes = listOf(),
        nestedExtendList = listOf(),
        extensionsList = listOf(),
        reserveds = listOf(),
        options = Options(Options.FILE_OPTIONS, listOf()),
        syntax = PROTO_3,
    )
    val DEFAULT_FIELD = Field(
        namespaces = listOf(
            "ru.pixnews.igdbclient.model",
            "Game",
        ),
        location = Location.get("/datasource-igdb/src/main/proto/igdbapi.proto:470:5"),
        label = null,
        name = "id",
        documentation = "",
        tag = 1,
        default = null,
        elementType = "uint64",
        options = Options(Options.FIELD_OPTIONS, listOf()),
        isExtension = false,
        isOneOf = false,
        declaredJsonName = null,
    )
    val IGDB_CLIENT_DSL_STUB = SourceFile.kotlin(
        "IgdbClientDsl.kt",
        """
            package ru.pixnews.igdbclient.dsl

            @DslMarker
            @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
            public annotation class IgdbClientDsl
            """.trimIndent(),
    )
    val IGDB_REQUEST_FIELD_STUB = SourceFile.kotlin(
        "IgdbRequestField.kt",
        """
            package ru.pixnews.igdbclient.dsl.field

            import ru.pixnews.igdbclient.dsl.IgdbClientDsl
            import ru.pixnews.igdbclient.scheme.field.IgdbField

            @IgdbClientDsl
            public data class IgdbRequestField<out O : Any> internal constructor(
                public val igdbField: IgdbField<O>,
                public val parent: IgdbRequestField<*>? = null,
            ) {
                public val igdbFullName: String
                    get() = if (parent == null) {
                        this.igdbField.igdbName
                    } else {
                        parent.igdbFullName + "." + this.igdbField.igdbName
                    }

                override fun toString(): String = igdbFullName
            }
            """.trimIndent(),
    )
    val IGDB_FIELD_STUB = SourceFile.kotlin(
        "IgdbField.kt",
        """
        package ru.pixnews.igdbclient.scheme.field
        public interface IgdbField<out O : Any> {
            public val igdbName: String

            public companion object {
                public val ALL: IgdbField<Nothing> = IgdbFieldAll

                public object IgdbFieldAll : IgdbField<Nothing> {
                    override val igdbName: String = "*"
                    override fun toString(): String = igdbName
                }
            }
        }
        """.trimIndent(),
    )
    val IGDB_GAME_MODEL_STUB = SourceFile.kotlin(
        "Game.kt",
        """
                package ru.pixnews.igdbclient.model
                public class Game {
                    public companion object
                }
            """.trimIndent(),
    )
    val IGDB_AGE_RATING_MODEL_STUB = SourceFile.kotlin(
        "AgeRating.kt",
        """
                package ru.pixnews.igdbclient.model
                public class AgeRating {
                    public companion object
                }
            """.trimIndent(),
    )
    val IGDB_REQUEST_FIELD_DSL_STUB = SourceFile.kotlin(
        "IgdbRequestFieldDsl.kt",
        """
            package ru.pixnews.igdbclient.dsl.field

            import ru.pixnews.igdbclient.dsl.IgdbClientDsl
            import ru.pixnews.igdbclient.scheme.field.IgdbField

            @IgdbClientDsl
            public sealed class IgdbRequestFieldDsl<F : IgdbField<T>, out T : Any>(
                protected val parentIgdbField: IgdbRequestField<*>? = null,
            ) {
                public val all: IgdbRequestField<F> get() = IgdbRequestField(IgdbField.ALL, parentIgdbField)

                public fun fieldWithId(fieldId: F): IgdbRequestField<T> = IgdbRequestField(fieldId, parentIgdbField)
            }
            """.trimIndent(),
    )
    private val fieldTypeSetter = Field::class.declaredMemberProperties
        .filterIsInstance<KMutableProperty<ProtoType?>>()
        .first { it.name == "type" }
        .setter.apply {
            this.isAccessible = true
        }

    internal fun Field.forceSetType(type: ProtoType?): Field {
        fieldTypeSetter.call(this, type)
        return this
    }
}
