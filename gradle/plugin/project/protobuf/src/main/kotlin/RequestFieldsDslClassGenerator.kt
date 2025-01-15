/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.gradle.protobuf.igdb

import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.FEATURE_FLAG_WITH_BACKING_INSTANCE
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.FIELD_WITH_ID_METHOD_NAME
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDBCLIENT_MODEL_PACKAGE_NAME
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDB_DSL_CLASS
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDB_REQUEST_FIELDS_DSL_BASE_CLASS
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDB_REQUEST_FIELD_CLASS
import at.released.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.REQUEST_FIELDS_DSL_PACKAGE_NAME
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Type
import java.util.Locale
import kotlin.LazyThreadSafetyMode.NONE

internal class RequestFieldsDslClassGenerator(
    private val type: Type,
) : () -> GeneratedFileContent {
    private val outputFieldsClassName: ClassName = outputFieldDslClassName(type.name)
    private val fieldsClassName: ClassName = SchemeEnumClassGenerator.outputEnumSchemeClassName(type.name)
    private val outputFileName = outputFieldsClassName.simpleName
    private val igdbclientModel = ClassName(IGDBCLIENT_MODEL_PACKAGE_NAME, type.name)
    private val igdbclientModelCompanion = ClassName(IGDBCLIENT_MODEL_PACKAGE_NAME, type.name, "Companion")
    private val fieldsReturnType = IGDB_REQUEST_FIELD_CLASS.parameterizedBy(igdbclientModel)

    /**
     * ```
     * private val _gameFieldsInstance = IgdbGameFields()
     * ```
     */
    private val backingInstance: PropertySpec = PropertySpec.builder(
        "_${outputFieldsClassName.simpleName.replaceFirstChar { it.lowercase(Locale.ROOT) }}Instance",
        outputFieldsClassName,
    )
        .addModifiers(PRIVATE)
        .initializer("%T()", outputFieldsClassName)
        .build()

    /**
     * ```
     * public val Game.Companion.field: IgdbGameFields get() = _gameFieldsInstance
     * ```
     */
    private val classCompanionFieldFactory: PropertySpec by lazy(NONE) {
        PropertySpec.builder("field", outputFieldsClassName)
            .receiver(igdbclientModelCompanion)
            .addModifiers(PUBLIC)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %N", backingInstance)
                    .build(),
            )
            .build()
    }
    private val classCompanionFieldFactoryNoBackingField: PropertySpec by lazy(NONE) {
        PropertySpec.builder("field", outputFieldsClassName)
            .receiver(igdbclientModelCompanion)
            .addModifiers(PUBLIC)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %T()", outputFieldsClassName)
                    .build(),
            )
            .build()
    }
    private val parentConstructorParameter = ParameterSpec.builder(
        "parentIgdbField",
        IGDB_REQUEST_FIELD_CLASS.parameterizedBy(STAR).copy(nullable = true),
    )
        .defaultValue("null")
        .build()

    override fun invoke(): GeneratedFileContent = GeneratedFileContent(
        filePath = getFieldsClassPath(type.type),
        content = FileSpec
            .builder(REQUEST_FIELDS_DSL_PACKAGE_NAME, outputFileName)
            .apply {
                if (FEATURE_FLAG_WITH_BACKING_INSTANCE) {
                    addProperty(backingInstance)
                    addProperty(classCompanionFieldFactory)
                } else {
                    addProperty(classCompanionFieldFactoryNoBackingField)
                }
            }
            .addType(generateFieldsClass())
            .build()
            .toString(),
    )

    /**
     * ```
     * public class GameFieldDsl internal constructor()...
     * ```
     */
    private fun generateFieldsClass(): TypeSpec {
        val primaryConstructor = FunSpec.constructorBuilder()
            .addModifiers(INTERNAL)
            .addParameter(parentConstructorParameter)
            .build()

        val classBuilder = TypeSpec.classBuilder(outputFieldsClassName)
            .addModifiers(PUBLIC)
            .addAnnotation(IGDB_DSL_CLASS)
            .primaryConstructor(primaryConstructor)
            .superclass(
                IGDB_REQUEST_FIELDS_DSL_BASE_CLASS
                    .parameterizedBy(fieldsClassName, igdbclientModel),
            )
            .addSuperclassConstructorParameter("%N", parentConstructorParameter)
            .addClassDocumentation()

        when (type) {
            is MessageType -> classBuilder.addProperties(type.declaredFields.map(::generateProperty))
            is EnclosingType -> Unit
            else -> Unit
        }

        return classBuilder.build()
    }

    private fun TypeSpec.Builder.addClassDocumentation(): TypeSpec.Builder {
        val igdbReferenceUrl = IgdbFieldsDslGeneratorPaths.getIgdbEntityReferenceUrl(type)
        return addKdoc(
            """
            | Dsl to generate a query string for a field of the [%T] IGDB entity.
            |
            | See [%L](%L)
            """.trimMargin(),
            igdbclientModel,
            igdbReferenceUrl,
            igdbReferenceUrl,
        )
    }

    /**
     * ```
     * public val id: IgdbRequestField<Game> get() = IgdbRequestField(GameField.ID, parentIgdbField)
     * public val age_ratings: AgeRatingFields<Game> get() =
     *     AgeRatingFields(IgdbRequestField(GameField.AGE_RATINGS, parentIgdbField))
     * ```
     */
    private fun generateProperty(field: Field): PropertySpec {
        val returnType: TypeName
        val getter: FunSpec
        val documentationBuilder: PropertySpec.Builder.() -> PropertySpec.Builder

        val enumFieldRef = fieldsClassName.nestedClass(field.name.uppercase())

        if (field.isIgdbObjectModel()) {
            val fieldTypeName = field.type?.simpleName ?: error("field.type not set")
            val fieldDslClass = outputFieldDslClassName(fieldTypeName)
            returnType = fieldDslClass
            getter = FunSpec.getterBuilder()
                .addStatement(
                    "return %T(%L(%T))",
                    fieldDslClass,
                    FIELD_WITH_ID_METHOD_NAME,
                    enumFieldRef,
                )
                .build()
            documentationBuilder = {
                val fieldType = ClassName(IGDBCLIENT_MODEL_PACKAGE_NAME, fieldTypeName)
                addKdoc(
                    """
                    | Fields of the %S ([%L][%T]) nested IGDB entity
                """.trimMargin(),
                    field.name,
                    fieldType.simpleName,
                    fieldType,
                )
            }
        } else {
            returnType = fieldsReturnType
            getter = FunSpec.getterBuilder()
                .addStatement(
                    "return %L(%T)",
                    FIELD_WITH_ID_METHOD_NAME,
                    enumFieldRef,
                )
                .build()
            documentationBuilder = {
                addKdoc(
                    """
                    | Query for the %S field of the [%T] Igdb entity.
                    |
                    | The value will be returned in the [%T] field of the response
                """.trimMargin(),
                    field.name,
                    igdbclientModel,
                    igdbclientModel.nestedClass(field.name),
                )
            }
        }

        return PropertySpec.builder(field.name, returnType, PUBLIC)
            .getter(getter)
            .apply {
                documentationBuilder(this)
            }
            .build()
    }

    internal companion object {
        /** Returns a path like `igdb/field/GameFieldDsl.kt`. */
        private fun getFieldsClassPath(protoType: ProtoType): List<String> =
            REQUEST_FIELDS_DSL_PACKAGE_NAME.split(".") + listOf(
                protoType.simpleName + "FieldDsl.kt",
            )

        private fun outputFieldDslClassName(typeName: String): ClassName =
            ClassName(REQUEST_FIELDS_DSL_PACKAGE_NAME, typeName + "FieldDsl")

        private fun Field.isIgdbObjectModel(): Boolean = this.type?.let { type ->
            when {
                type.toString().endsWith("Enum") -> false
                type.enclosingTypeOrPackage == IGDBCLIENT_MODEL_PACKAGE_NAME -> true
                else -> false
            }
        } ?: false
    }
}
