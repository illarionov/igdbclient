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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.tableOf
import ru.pixnews.igdbclient.model.IgdbImageFormat
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
import kotlin.test.Test

@Suppress("AVOID_USING_UTILITY_CLASS")
class IgdbImageUtilTest {
    @Test
    fun igdbImageUrl_should_throw_exception_on_incorrect_image_id() {
        assertFailure {
            igdbImageUrl(imageId = " !@#$%^&*()_+-'\"'")
        }.isInstanceOf<IllegalArgumentException>()
    }

    @Test
    fun igdbImageUrl_should_return_correct_URL_depending_on_image_size() = tableOf("Size", "SubPath")
        .row(COVER_SMALL, "t_cover_small")
        .row(COVER_BIG, "t_cover_big")
        .row(LOGO_MEDIUM, "t_logo_med")
        .row(SCREENSHOT_MEDIUM, "t_screenshot_med")
        .row(SCREENSHOT_BIG, "t_screenshot_big")
        .row(SCREENSHOT_HUGE, "t_screenshot_huge")
        .row(THUMB, "t_thumb")
        .row(MICRO, "t_micro")
        .row(H720P, "t_720p")
        .row(H1080P, "t_1080p")
        .forAll { imageSize, subPath ->
            val urlSize1x = igdbImageUrl(
                imageId = "em1y2ugcwy2myuhvb9db",
                imageSize = imageSize,
                size2x = false,
                imageFormat = IgdbImageFormat.JPEG,
            )

            val urlSize2x = igdbImageUrl(
                imageId = "em1y2ugcwy2myuhvb9db",
                imageSize = imageSize,
                size2x = true,
                imageFormat = IgdbImageFormat.JPEG,
            )

            assertThat(urlSize1x)
                .isEqualTo("https://images.igdb.com/igdb/image/upload/$subPath/em1y2ugcwy2myuhvb9db.jpg")
            assertThat(urlSize2x)
                .isEqualTo("https://images.igdb.com/igdb/image/upload/${subPath}_2x/em1y2ugcwy2myuhvb9db.jpg")
        }

    @Test
    fun igdbImageUrl_should_return_correct_URL_depending_on_image_format() = tableOf("Format", "SubPath")
        .row(IgdbImageFormat.JPEG, ".jpg")
        .row(IgdbImageFormat.WEBP, ".webp")
        .row(IgdbImageFormat.PNG, ".png")
        .row(IgdbImageFormat.GIF, ".gif")
        .forAll { imageFormat, extension ->
            val url = igdbImageUrl(
                imageId = "em1y2ugcwy2myuhvb9db",
                imageSize = LOGO_MEDIUM,
                imageFormat = imageFormat,
                size2x = false,
            )
            assertThat(url)
                .isEqualTo("https://images.igdb.com/igdb/image/upload/t_logo_med/em1y2ugcwy2myuhvb9db$extension")
        }
}
