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
package ru.pixnews.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.containsExactly
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.countEndpoint
import ru.pixnews.igdbclient.apicalypse.apicalypseMultiQuery
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgnoreAndroid
import ru.pixnews.igdbclient.library.test.IgnoreJs
import ru.pixnews.igdbclient.library.test.IgnoreNative
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.Platform
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult
import kotlin.test.Test

@IgnoreAndroid
@IgnoreJs
@IgnoreNative
internal class MultiQueryArrayParserTest {
    @Test
    fun multiQueryArrayParser_should_parse_multi_query_responses() {
        val query = apicalypseMultiQuery {
            query(IgdbEndpoint.PLATFORM.countEndpoint(), "Count of Platforms") {}
            query(IgdbEndpoint.GAME, "Playstation Games") {
                fields("name", "category", "platforms.name")
                where("platforms !=n ")
                limit(5)
            }
        }

        val parser = MultiQueryArrayParser

        val result = parser.parse(
            query,
            Fixtures.MockIgdbResponseContent.multiQueryPlatformsCountPsGames,
        )

        assertThat(result).containsExactly(
            UnpackedMultiQueryResult<Any>(
                name = "Count of Platforms",
                count = 200,
                results = null,
            ),
            UnpackedMultiQueryResult<Any>(
                name = "Playstation Games",
                count = 0,
                results = listOf(
                    Game(
                        id = 176032,
                        name = "Nick Quest",
                        platforms = listOf(
                            Platform(id = 6, name = "PC (Microsoft Windows)"),
                        ),
                    ),
                    Game(
                        id = 50975,
                        name = "Storybook Workshop",
                        platforms = listOf(
                            Platform(id = 5, name = "Wii"),
                        ),
                    ),
                ),
            ),
        )
    }
}
