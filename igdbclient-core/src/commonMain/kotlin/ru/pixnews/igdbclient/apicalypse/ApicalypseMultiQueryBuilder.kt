/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.apicalypse

import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.apicalypse.ApicalypseMultiQuery.Subquery

/**
 * APICalypse multi-query builder.
 *
 * See [Multi-query](https://api-docs.igdb.com/#multi-query)
 */
@ApicalypseDsl
public class ApicalypseMultiQueryBuilder {
    private val subqueries: MutableList<Subquery> = mutableListOf()

    public fun query(
        endpoint: IgdbEndpoint<*>,
        resultName: String,
        builder: ApicalypseQueryBuilder.() -> Unit,
    ) {
        check(subqueries.size < MAX_SUB_QUERIES) {
            "No more than $MAX_SUB_QUERIES subqueries allowed"
        }
        subqueries += Subquery(
            endpoint,
            resultName,
            apicalypseQuery(builder),
        )
    }

    public fun build(): ApicalypseMultiQuery {
        val query = subqueries.joinToString(separator = "\n") { subQuery ->
            """query ${subQuery.endpoint.endpoint} "${subQuery.resultName}" {${subQuery.query}};"""
        }
        return object : ApicalypseMultiQuery {
            override val subqueries: List<Subquery> = this@ApicalypseMultiQueryBuilder.subqueries.toList()
            override fun toString(): String = query
        }
    }

    private companion object {
        const val MAX_SUB_QUERIES = 10
    }
}
