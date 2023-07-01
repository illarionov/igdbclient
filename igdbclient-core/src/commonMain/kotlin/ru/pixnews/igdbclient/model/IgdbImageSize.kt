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
package ru.pixnews.igdbclient.model

public enum class IgdbImageSize(
    public val igdbId: String,
) {
    /**
     * Size: 90 x 128 (45:64), Scale type: Fit
     */
    COVER_SMALL("t_cover_small"),

    /**
     * Size: 264 x 374 (45:64), Scale type: Fit
     */
    COVER_BIG("t_cover_big"),

    /**
     * Size: 284 x 160 (16:9), Scale type: Fit
     */
    LOGO_MEDIUM("t_logo_med"),

    /**
     * Size: 569 x 320 (16:9), Scale type: Lfill, Center gravity
     */
    SCREENSHOT_MEDIUM("t_screenshot_med"),

    /**
     * Size: 889 x 500 (16:9), Scale type: Lfill, Center gravity
     */
    SCREENSHOT_BIG("t_screenshot_big"),

    /**
     * Size: 1280 x 720 (16:9), Scale type: Lfill, Center gravity
     */
    SCREENSHOT_HUGE("t_screenshot_huge"),

    /**
     * Size: 90 x 90 (1:1), Scale type: Thumb, Center gravity
     */
    THUMB("t_thumb"),

    /**
     * Size: 35 x 35 (1:1), Scale type: Thumb, Center gravity
     */
    MICRO("t_micro"),

    /**
     * Size: 1280 x 720 (16:9), Scale type: Fit, Center gravity
     */
    H720P("t_720p"),

    /**
     * Size: 1920 x 1080 (16:9), Scale type: Fit, Center gravity
     */
    H1080P("t_1080p"),
}
