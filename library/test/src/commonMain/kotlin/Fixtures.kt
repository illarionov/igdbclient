/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.library.test

import okio.Buffer

public object Fixtures {
    public const val TEST_CLIENT_ID: String = "blev3l45p6jsjy7yo829to012bv3cp"
    public const val TEST_TOKEN: String = "abcd5gq1rtwyp2vq47r8evh7lp4eaa"

    public object MockIgdbResponseContent {
        private const val MOCK_RESPONSES_PATH = "/mock/api.igdb.com"
        public val gamesSearch: Buffer
            get() = readResourceAsBuffer("$MOCK_RESPONSES_PATH/games/games_search_diablo_limit_5.pb")

        public val authFailure: Buffer
            get() = readResourceAsBuffer("$MOCK_RESPONSES_PATH/401_auth_failure.json")

        public val syntaxError: Buffer
            get() = readResourceAsBuffer("$MOCK_RESPONSES_PATH/400_syntax_error.json")

        public val multiQueryPlatformsCountPsGames: Buffer
            get() = readResourceAsBuffer("$MOCK_RESPONSES_PATH/multiquery/multiquery_count_ps_games.pb")

        public val countGames: Buffer
            get() = Buffer().apply { write(byteArrayOf(0x08, 0x22)) }
    }
}
