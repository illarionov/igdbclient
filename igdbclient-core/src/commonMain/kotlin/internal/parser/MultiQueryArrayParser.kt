/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.apicalypse.ApicalypseMultiQuery
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.model.MultiQueryResult
import at.released.igdbclient.model.MultiQueryResultArray
import at.released.igdbclient.model.UnpackedMultiQueryResult
import okio.Buffer
import okio.BufferedSource

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
