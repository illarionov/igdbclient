/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import at.released.igdbclient.IgdbDumpApi
import at.released.igdbclient.IgdbResult
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.internal.IgdbRequest.GetRequest
import at.released.igdbclient.internal.parser.IgdbParser
import at.released.igdbclient.internal.parser.igdbDumpJsonParser
import at.released.igdbclient.internal.parser.igdbDumpSummaryListJsonParser
import at.released.igdbclient.model.dump.IgdbDump
import at.released.igdbclient.model.dump.IgdbDumpSummary

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
