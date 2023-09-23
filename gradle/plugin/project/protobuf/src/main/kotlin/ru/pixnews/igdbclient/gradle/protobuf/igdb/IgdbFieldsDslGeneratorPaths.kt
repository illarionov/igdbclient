/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.gradle.protobuf.igdb

import com.squareup.kotlinpoet.ClassName
import com.squareup.wire.schema.Type

internal object IgdbFieldsDslGeneratorPaths {
    const val FEATURE_FLAG_WITH_BACKING_INSTANCE = true
    const val REQUEST_FIELDS_DSL_PACKAGE_NAME = "ru.pixnews.igdbclient.dsl.field"
    const val SCHEME_PACKAGE_NAME = "ru.pixnews.igdbclient.scheme.field"
    const val IGDBCLIENT_MODEL_PACKAGE_NAME = "ru.pixnews.igdbclient.model"
    const val FIELD_WITH_ID_METHOD_NAME = "fieldWithId"
    val IGDB_DSL_CLASS = ClassName("ru.pixnews.igdbclient.dsl", "IgdbClientDsl")
    val IGDB_REQUEST_FIELD_CLASS = ClassName(REQUEST_FIELDS_DSL_PACKAGE_NAME, "IgdbRequestField")
    val IGDB_FIELD_INTERFACE = ClassName(SCHEME_PACKAGE_NAME, "IgdbField")
    val IGDB_REQUEST_FIELDS_DSL_BASE_CLASS = ClassName(REQUEST_FIELDS_DSL_PACKAGE_NAME, "IgdbRequestFieldDsl")
    val EXCLUDED_MESSAGES: Set<String> = setOf(
        "Count",
        "InternalTwitchTokenProto",
        "MultiQueryResult",
        "MultiQueryResultArray",
    )

    fun getIgdbEntityReferenceUrl(type: Type): String = "https://api-docs.igdb.com/#${type.toReferenceUrlAnchorCase()}"

    fun Type.toReferenceUrlAnchorCase(): String = buildString {
        type.simpleName.forEachIndexed { index, char ->
            when {
                char in 'A'..'Z' -> {
                    if (index != 0) {
                        append("-")
                    }
                    append(char.lowercaseChar())
                }
                else -> append(char)
            }
        }
    }
}
