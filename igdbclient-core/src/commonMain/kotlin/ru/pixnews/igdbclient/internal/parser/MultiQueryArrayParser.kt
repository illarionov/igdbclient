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
package ru.pixnews.igdbclient.internal.parser

import okio.Buffer
import okio.BufferedSource
import ru.pixnews.igdbclient.apicalypse.ApicalypseMultiQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.model.MultiQueryResult
import ru.pixnews.igdbclient.model.MultiQueryResultArray
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult

internal object MultiQueryArrayParser {
    fun parse(query: ApicalypseQuery, source: BufferedSource): List<UnpackedMultiQueryResult<*>> {
        val multiQuery = query as? ApicalypseMultiQuery ?: error("should be ApicalypseMultiQuery")
        val multiQueryResultArray = MultiQueryResultArray.ADAPTER.decode(source)
        return multiQueryResultArray.result.mapIndexed { subQueryIndex, subQueryResult: MultiQueryResult ->
            val endpoint = multiQuery.subqueries[subQueryIndex].endpoint
            UnpackedMultiQueryResult(
                name = subQueryResult.name,
                count = subQueryResult.count,
                results = subQueryResult.results.let { results ->
                    if (results.isNotEmpty()) {
                        val parser = checkNotNull(endpoint.singleItemParser) {
                            "No parser for `$endpoint`"
                        }
                        subQueryResult.results.map { payload -> parser(Buffer().write(payload)) }
                    } else {
                        null
                    }
                },
            )
        }
    }
}
