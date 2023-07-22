/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.util

import ru.pixnews.igdbclient.dsl.IgdbClientConfigBlock.Companion.IGDB_IMAGE_URL
import ru.pixnews.igdbclient.model.Artwork
import ru.pixnews.igdbclient.model.CharacterMugShot
import ru.pixnews.igdbclient.model.CompanyLogo
import ru.pixnews.igdbclient.model.Cover
import ru.pixnews.igdbclient.model.GameEngineLogo
import ru.pixnews.igdbclient.model.IgdbImageFormat
import ru.pixnews.igdbclient.model.IgdbImageFormat.WEBP
import ru.pixnews.igdbclient.model.IgdbImageSize
import ru.pixnews.igdbclient.model.IgdbImageSize.H1080P
import ru.pixnews.igdbclient.model.PlatformLogo
import ru.pixnews.igdbclient.model.Screenshot

@Suppress("AVOID_USING_UTILITY_CLASS")
public fun igdbImageUrl(
    imageId: String,
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String {
    val encodedImageId = imageId.filter(Char::isLetterOrDigit)
    require(encodedImageId.isNotEmpty()) { "imageId should not be empty" }

    return buildString {
        append(IGDB_IMAGE_URL)
        append(imageSize.igdbId)
        if (size2x) {
            append("_2x")
        }
        append('/')
        append(encodedImageId)
        append('.')
        append(imageFormat.igdbId)
    }
}

public fun Artwork.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun CharacterMugShot.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun CompanyLogo.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun Cover.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun GameEngineLogo.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun PlatformLogo.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)

public fun Screenshot.imageUrl(
    imageSize: IgdbImageSize = H1080P,
    size2x: Boolean = true,
    imageFormat: IgdbImageFormat = WEBP,
): String = igdbImageUrl(this.image_id, imageSize, size2x, imageFormat)
