/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal

import ru.pixnews.igdbclient.IgdbDumpApi
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest.GetRequest
import ru.pixnews.igdbclient.internal.parser.IgdbParser
import ru.pixnews.igdbclient.internal.parser.igdbDumpJsonParser
import ru.pixnews.igdbclient.internal.parser.igdbDumpSummaryListJsonParser
import ru.pixnews.igdbclient.model.dump.IgdbDump
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary

internal class IgdbDumpApiImplementation(
    val requestExecutor: RequestExecutor,
) : IgdbDumpApi {
    override suspend fun getDumps(): IgdbResult<List<IgdbDumpSummary>, IgdbHttpErrorResponse> {
        val request = GetRequest(
            path = "dumps",
            queryParameters = mapOf(),
            successResponseParser = IgdbParser::igdbDumpSummaryListJsonParser,
        )
        return requestExecutor(request)
    }

    override suspend fun getDump(endpoint: String): IgdbResult<IgdbDump, IgdbHttpErrorResponse> {
        val request = GetRequest(
            path = "dumps/$endpoint",
            queryParameters = mapOf(),
            successResponseParser = IgdbParser::igdbDumpJsonParser,
        )
        return requestExecutor(request)
    }
}
