/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient

import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.model.dump.IgdbDump
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary

/**
 * IgdbDumpApi allows you to get CSV Data Dumps
 *
 * See [https://api-docs.igdb.com/#data-dumps](https://api-docs.igdb.com/#data-dumps)
 */
public interface IgdbDumpApi {
    /**
     * Returns a list of available Data Dumps
     */
    public suspend fun getDumps(): IgdbResult<List<IgdbDumpSummary>, IgdbHttpErrorResponse>

    /**
     * Returns S3 download link for the CSV dump of [endpoint]
     */
    public suspend fun getDump(endpoint: String): IgdbResult<IgdbDump, IgdbHttpErrorResponse>
}

/**
 * Returns S3 download link for the CSV dump of [endpoint]
 */
public suspend fun IgdbDumpApi.getDump(
    endpoint: IgdbEndpoint<*>,
): IgdbResult<IgdbDump, IgdbHttpErrorResponse> = getDump(endpoint.endpoint)
