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
package ru.pixnews.igdbclient.internal.multiquery

import okio.Buffer
import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.apicalypse.ApicalypseMultiQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.model.MultiQueryResult
import ru.pixnews.igdbclient.model.MultiQueryResultArray
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult

@InternalIgdbClientApi
internal class MultiQueryArrayParser(
    private val resultArrayParser: (BufferedSource) -> MultiQueryResultArray = MultiQueryResultArray.ADAPTER::decode,
) : (ApicalypseQuery, BufferedSource) -> List<UnpackedMultiQueryResult<*>> {
    override fun invoke(query: ApicalypseQuery, inputStream: BufferedSource): List<UnpackedMultiQueryResult<*>> {
        val multiQuery = query as? ApicalypseMultiQuery ?: error("should be ApicalypseMultiQuery")
        val multiQueryResultArray = resultArrayParser(inputStream)
        return multiQueryResultArray.result.mapIndexed { subqueryIndex, subqueryResult: MultiQueryResult ->
            val endpoint = multiQuery.subqueries[subqueryIndex].endpoint
            UnpackedMultiQueryResult(
                name = subqueryResult.name,
                count = subqueryResult.count,
                results = subqueryResult.results.let { results ->
                    if (results.isNotEmpty()) {
                        val parser = checkNotNull(endpoint.singleItemParser) {
                            "No parser for `$endpoint`"
                        }
                        subqueryResult.results.map { payload -> parser(Buffer().write(payload)) }
                    } else {
                        null
                    }
                },
            )
        }
    }
}
