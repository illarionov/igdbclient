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
package ru.pixnews.igdbclient.library.test

import okhttp3.mockwebserver.MockResponse
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

        public fun createSuccessMockResponse(): MockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/protobuf")
            .setBody(gamesSearch)
    }
}
