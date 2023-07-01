/*
 * Copyright 2023 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pixnews.igdbclient.apicalypse

import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.apicalypse.ApicalypseMultiQuery.Subquery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery

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
