/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.library.test

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Verbose

public object TestingLoggers {
    public val consoleLogger: Logger = Logger(
        config = object : LoggerConfig {
            override val logWriterList: List<LogWriter> = listOf(testLogWriter)
            override val minSeverity: Severity = Verbose
        },
    )
}
