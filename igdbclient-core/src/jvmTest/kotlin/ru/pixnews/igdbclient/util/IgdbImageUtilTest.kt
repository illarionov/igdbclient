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
package ru.pixnews.igdbclient.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.igdbclient.model.IgdbImageFormat
import ru.pixnews.igdbclient.model.IgdbImageSize
import ru.pixnews.igdbclient.model.IgdbImageSize.COVER_BIG
import ru.pixnews.igdbclient.model.IgdbImageSize.COVER_SMALL
import ru.pixnews.igdbclient.model.IgdbImageSize.H1080P
import ru.pixnews.igdbclient.model.IgdbImageSize.H720P
import ru.pixnews.igdbclient.model.IgdbImageSize.LOGO_MEDIUM
import ru.pixnews.igdbclient.model.IgdbImageSize.MICRO
import ru.pixnews.igdbclient.model.IgdbImageSize.SCREENSHOT_BIG
import ru.pixnews.igdbclient.model.IgdbImageSize.SCREENSHOT_HUGE
import ru.pixnews.igdbclient.model.IgdbImageSize.SCREENSHOT_MEDIUM
import ru.pixnews.igdbclient.model.IgdbImageSize.THUMB

class IgdbImageUtilTest {
    @Test
    fun `igdbImageUrl() should throw exception on incorrect image id`() {
        shouldThrow<IllegalArgumentException> {
            igdbImageUrl(imageId = " !@#$%^&*()_+-'\"'")
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("testSource")
    fun `igdbImageUrl() should return correct URL on correct data`(testSpec: IgdbUrlTestData) {
        val url = igdbImageUrl(
            imageId = testSpec.imageId,
            imageSize = testSpec.imageSize,
            size2x = testSpec.size2x,
            imageFormat = testSpec.imageFormat,
        )

        url shouldBe testSpec.expectedUrl
    }

    data class IgdbUrlTestData(
        val imageId: String,
        val imageSize: IgdbImageSize = H1080P,
        val size2x: Boolean = false,
        val imageFormat: IgdbImageFormat = IgdbImageFormat.JPEG,
        val expectedUrl: String,
    )

    companion object {
        @JvmStatic
        fun testSource(): List<IgdbUrlTestData> = buildList {
            listOf(
                COVER_SMALL to "t_cover_small",
                COVER_BIG to "t_cover_big",
                LOGO_MEDIUM to "t_logo_med",
                SCREENSHOT_MEDIUM to "t_screenshot_med",
                SCREENSHOT_BIG to "t_screenshot_big",
                SCREENSHOT_HUGE to "t_screenshot_huge",
                THUMB to "t_thumb",
                MICRO to "t_micro",
                H720P to "t_720p",
                H1080P to "t_1080p",
            ).flatMap { (size, subPath) ->
                listOf(
                    IgdbUrlTestData(
                        imageId = "em1y2ugcwy2myuhvb9db",
                        imageSize = size,
                        size2x = false,
                        expectedUrl = "https://images.igdb.com/igdb/image/upload/$subPath/em1y2ugcwy2myuhvb9db.jpg",
                    ),
                    @Suppress("MaxLineLength")
                    IgdbUrlTestData(
                        imageId = "em1y2ugcwy2myuhvb9db",
                        imageSize = size,
                        size2x = true,
                        expectedUrl = "https://images.igdb.com/igdb/image/upload/${subPath}_2x/em1y2ugcwy2myuhvb9db.jpg",
                    ),
                )
            }.forEach { add(it) }

            listOf(
                IgdbImageFormat.JPEG to ".jpg",
                IgdbImageFormat.WEBP to ".webp",
                IgdbImageFormat.PNG to ".png",
                IgdbImageFormat.GIF to ".gif",
            ).map { (format, extension) ->
                IgdbUrlTestData(
                    imageId = "em1y2ugcwy2myuhvb9db",
                    imageSize = LOGO_MEDIUM,
                    imageFormat = format,
                    size2x = false,
                    expectedUrl = "https://images.igdb.com/igdb/image/upload/t_logo_med/em1y2ugcwy2myuhvb9db$extension",
                )
            }.forEach { add(it) }
        }
    }
}
