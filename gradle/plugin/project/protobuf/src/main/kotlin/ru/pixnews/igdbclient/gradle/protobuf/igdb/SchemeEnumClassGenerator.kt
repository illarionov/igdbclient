/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.protobuf.igdb

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Type
import ru.pixnews.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDBCLIENT_MODEL_PACKAGE_NAME
import ru.pixnews.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.IGDB_FIELD_INTERFACE
import ru.pixnews.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.SCHEME_PACKAGE_NAME
import ru.pixnews.igdbclient.gradle.protobuf.igdb.IgdbFieldsDslGeneratorPaths.getIgdbEntityReferenceUrl
import java.util.Locale

/**
 * Generator of enum class with all fields of the Igdb object type
 */
internal class SchemeEnumClassGenerator(
    private val type: Type,
) : () -> GeneratedFileContent {
    private val outputClassName: ClassName = outputEnumSchemeClassName(type.name)
    private val outputFileName = outputClassName.simpleName
    private val igdbclientModel = ClassName(IGDBCLIENT_MODEL_PACKAGE_NAME, type.name)

    override fun invoke(): GeneratedFileContent = GeneratedFileContent(
        filePath = getEnumSchemeClassPath(type.type),
        content = FileSpec
            .builder(SCHEME_PACKAGE_NAME, outputFileName)
            .addType(generateEnumSchemeClass())
            .build()
            .toString(),
    )

    private fun generateEnumSchemeClass(): TypeSpec {
        val igdbnameParameter = ParameterSpec.builder("igdbName", String::class)
            .build()

        val enumBuilder = TypeSpec.enumBuilder(outputClassName)
            .addModifiers(PUBLIC)
            .addSuperinterface(IGDB_FIELD_INTERFACE.parameterizedBy(igdbclientModel))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(igdbnameParameter)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder(igdbnameParameter.name, igdbnameParameter.type, OVERRIDE, PUBLIC)
                    .initializer(igdbnameParameter.name)
                    .build(),
            )
            .addClassDocumentation()

        when (type) {
            is MessageType -> enumBuilder.addFieldsAsEnumConstants(type.declaredFields)
            is EnclosingType -> Unit
            else -> Unit
        }

        enumBuilder.addFunction(
            FunSpec.builder("toString")
                .addModifiers(OVERRIDE)
                .addStatement("return %N", igdbnameParameter)
                .returns(String::class)
                .build(),
        )

        return enumBuilder.build()
    }

    private fun TypeSpec.Builder.addClassDocumentation(): TypeSpec.Builder {
        val igdbReferenceUrl = getIgdbEntityReferenceUrl(type)
        return addKdoc(
            """
            | Fields of the [%T] IGDB entity.
            |
            | See [%L](%L)
            """.trimMargin(),
            igdbclientModel,
            igdbReferenceUrl,
            igdbReferenceUrl,
        )
    }

    private fun TypeSpec.Builder.addFieldsAsEnumConstants(fields: List<Field>) = fields.forEach { field ->
        val enumConstantName = field.name.uppercase(Locale.ROOT)
        val typeSpec = TypeSpec.anonymousClassBuilder()
            .addSuperclassConstructorParameter("%S", field.name)
            .addFieldDocumentation(field)
            .build()
        addEnumConstant(enumConstantName, typeSpec)
    }

    private fun TypeSpec.Builder.addFieldDocumentation(field: Field): TypeSpec.Builder {
        return addKdoc(
            """
            | Field %S of the [%T] IGDB entity. Matches [%T].
        """.trimMargin(),
            field.name,
            igdbclientModel,
            igdbclientModel.nestedClass(field.name),
        )
    }

    internal companion object {
        /** Returns a path like `igdb/field/scheme/GameField.kt`. */
        private fun getEnumSchemeClassPath(protoType: ProtoType): List<String> =
            SCHEME_PACKAGE_NAME.split(".") + listOf(
                protoType.simpleName + "Field.kt",
            )

        internal fun outputEnumSchemeClassName(typeName: String) = ClassName(SCHEME_PACKAGE_NAME, typeName + "Field")
    }
}
