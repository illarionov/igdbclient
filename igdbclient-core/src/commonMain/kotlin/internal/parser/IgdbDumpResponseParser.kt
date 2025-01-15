/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import at.released.igdbclient.InternalIgdbClientApi
import at.released.igdbclient.model.dump.IgdbDump
import at.released.igdbclient.model.dump.IgdbDumpSummary
import okio.BufferedSource

@InternalIgdbClientApi
internal expect fun IgdbParser.igdbDumpSummaryListJsonParser(source: BufferedSource): List<IgdbDumpSummary>

@InternalIgdbClientApi
internal expect fun IgdbParser.igdbDumpJsonParser(source: BufferedSource): IgdbDump
