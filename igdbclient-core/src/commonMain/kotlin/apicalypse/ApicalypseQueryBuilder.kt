/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.apicalypse

import ru.pixnews.igdbclient.apicalypse.SortOrder.ASC
import ru.pixnews.igdbclient.dsl.field.IgdbRequestField
import kotlin.jvm.JvmInline

/**
 * APICalypse query builder.
 *
 * See [APICalypse cheatsheet](https://api-docs.igdb.com/#apicalypse-1)
 */
@ApicalypseDsl
public class ApicalypseQueryBuilder {
    private val fields: MutableList<ApiField> = mutableListOf()
    private val exclude: MutableList<ApiField> = mutableListOf()
    private var where: String? = null
    private var limit: Int? = null
    private var offset: Int? = null
    private var sort: SortOrderField? = null
    private var search: String? = null

    /**
     * Fields that should be returned as a result of an API request.
     *
     * Set to '*'  to return all the fields
     *
     * See [https://api-docs.igdb.com/#fields](https://api-docs.igdb.com/#fields)
     */
    public fun fields(vararg fieldList: String): ApicalypseQueryBuilder {
        fields.clear()
        fields.addAll(fieldList.map(::ApiField))
        return this
    }

    /**
     * Fields that should be returned as a result of an API request.
     *
     * Use [Game.field][ru.pixnews.igdbclient.model.Game.Companion.field] extension function to start building a
     * field request for the Game object.
     *
     * For example, you can use `Game.field.all` to query all fields
     *
     * See [https://api-docs.igdb.com/#fields](https://api-docs.igdb.com/#fields)
     */
    public fun fields(vararg fieldList: IgdbRequestField<*>): ApicalypseQueryBuilder {
        fields.clear()
        fields.addAll(fieldList.map(::ApiField))
        return this
    }

    /**
     * Fields to exclude when using '*' wildcard when specifying the list of [fields]
     *
     * See [https://api-docs.igdb.com/#exclude](https://api-docs.igdb.com/#exclude)
     */
    public fun exclude(vararg excludes: String): ApicalypseQueryBuilder {
        exclude.clear()
        exclude += excludes.map(::ApiField)
        return this
    }

    /**
     * Fields to exclude when using '*' wildcard when specifying the list of [fields]
     *
     * Use [Game.field][ru.pixnews.igdbclient.model.Game.Companion.field] extension function to start building a
     * field request for the Game object.
     *
     * See [https://api-docs.igdb.com/#exclude](https://api-docs.igdb.com/#exclude)
     */
    public fun exclude(vararg excludes: IgdbRequestField<*>): ApicalypseQueryBuilder {
        exclude.clear()
        exclude += excludes.map(::ApiField)
        return this
    }

    /**
     * SQL-like query filter.
     *
     * [where] value is expected to be pre-escaped as it will be added to the request in raw form.
     *
     * See [https://api-docs.igdb.com/#filters](https://api-docs.igdb.com/#filters)
     */
    public fun where(where: String): ApicalypseQueryBuilder {
        this.where = where
        return this
    }

    /**
     * Sets a limit on the number of results returned.
     *
     * Default limit is 10.
     *
     * See [https://api-docs.igdb.com/#pagination](https://api-docs.igdb.com/#pagination)
     */
    public fun limit(limit: Int): ApicalypseQueryBuilder {
        @Suppress("MagicNumber")
        require(limit in 1..500) {
            "Limit should be between 1 and 500"
        }
        this.limit = limit
        return this
    }

    /**
     * Number of results to skip over.
     *
     * Default value: 0
     *
     * See [https://api-docs.igdb.com/#search-1](https://api-docs.igdb.com/#search-1)
     */
    public fun offset(offset: Int): ApicalypseQueryBuilder {
        require(offset >= 0) {
            "Offset should be non-negative"
        }
        this.offset = offset
        return this
    }

    /**
     * Search based on name, results are sorted by similarity to the given search string.
     *
     * See [https://api-docs.igdb.com/#search-1](https://api-docs.igdb.com/#search-1)
     */
    public fun search(searchString: String): ApicalypseQueryBuilder {
        this.search = searchString
        return this
    }

    /**
     * Sets the field that will be used to sort the result and the sort order
     *
     * See [https://api-docs.igdb.com/#sorting](https://api-docs.igdb.com/#sorting)
     */
    public fun sort(field: String, order: SortOrder = ASC): ApicalypseQueryBuilder {
        this.sort = SortOrderField(ApiField(field), order)
        return this
    }

    /**
     * Builds the specified query.
     */
    public fun build(): ApicalypseQuery {
        val query = buildString {
            search?.let {
                append("search \"", quoteString(it), "\";")
            }
            if (fields.isNotEmpty()) {
                fields.joinTo(
                    buffer = this,
                    prefix = "f ",
                    separator = ",",
                    postfix = ";",
                    transform = ApiField::name,
                )
            }
            if (exclude.isNotEmpty()) {
                exclude.joinTo(
                    buffer = this,
                    prefix = "x ",
                    separator = ",",
                    postfix = ";",
                    transform = ApiField::name,
                )
            }
            where?.let {
                append("w ", it, ';')
            }
            limit?.let {
                append("l ", it, ';')
            }
            offset?.let {
                append("o ", it, ';')
            }
            sort?.let {
                append("s ", it.toRequestSubstring(), ';')
            }
        }
        return object : ApicalypseQuery {
            override fun toString(): String = query
        }
    }

    private class SortOrderField(
        val field: ApiField,
        val sortOrder: SortOrder = SortOrder.ASC,
    ) {
        fun toRequestSubstring(): String = "${field.name} ${sortOrder.igdbToken}"
    }

    @JvmInline
    private value class ApiField(val name: String) {
        init {
            require(name matches FIELD_REGEX && name.split(".").all(String::isNotEmpty)) {
                "Incorrect field name `$name`"
            }
        }
        constructor(field: IgdbRequestField<*>) : this(field.igdbFullName)

        private companion object {
            val FIELD_REGEX = Regex("""^[a-z_*.]+$""")
        }
    }

    private companion object {
        @Suppress("MagicNumber")
        private fun quoteString(value: String): String {
            return buildString {
                value.forEach { character ->
                    when (character) {
                        '"', '\\', '/' -> append('\\', character)
                        '\t' -> append("\\t")
                        '\b' -> append("\\b")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\u000C' -> append("\\f")
                        else -> if (character.code <= 0x1f) {
                            append("\\u")
                            append(character.code.toString(16).padStart(4, '0'))
                        } else {
                            append(character)
                        }
                    }
                }
            }
        }
    }
}
