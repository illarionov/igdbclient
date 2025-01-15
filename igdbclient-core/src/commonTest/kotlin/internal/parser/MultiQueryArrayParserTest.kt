/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal.parser

import assertk.assertThat
import assertk.assertions.containsExactly
import at.released.igdbclient.IgdbEndpoint
import at.released.igdbclient.IgdbEndpoint.Companion.countEndpoint
import at.released.igdbclient.apicalypse.apicalypseMultiQuery
import at.released.igdbclient.library.test.Fixtures
import at.released.igdbclient.library.test.IgnoreAndroid
import at.released.igdbclient.library.test.IgnoreJs
import at.released.igdbclient.library.test.IgnoreNative
import at.released.igdbclient.model.Game
import at.released.igdbclient.model.Platform
import at.released.igdbclient.model.UnpackedMultiQueryResult
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
