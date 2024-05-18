/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal.parser

import okio.BufferedSource
import ru.pixnews.igdbclient.InternalIgdbClientApi
import ru.pixnews.igdbclient.model.dump.IgdbDump
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary

@InternalIgdbClientApi
internal expect fun IgdbParser.igdbDumpSummaryListJsonParser(source: BufferedSource): List<IgdbDumpSummary>

@InternalIgdbClientApi
internal expect fun IgdbParser.igdbDumpJsonParser(source: BufferedSource): IgdbDump
