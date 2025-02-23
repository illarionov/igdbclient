/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooManyFunctions")

package at.released.igdbclient

import at.released.igdbclient.IgdbResult.Failure.ApiFailure
import at.released.igdbclient.IgdbResult.Failure.HttpFailure
import at.released.igdbclient.IgdbResult.Failure.NetworkFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownFailure
import at.released.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import at.released.igdbclient.IgdbResult.Success
import at.released.igdbclient.apicalypse.ApicalypseMultiQueryBuilder
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.apicalypse.ApicalypseQueryBuilder
import at.released.igdbclient.apicalypse.apicalypseMultiQuery
import at.released.igdbclient.apicalypse.apicalypseQuery
import at.released.igdbclient.dsl.IgdbClientConfigBlock
import at.released.igdbclient.dsl.IgdbHttpEngineConfig
import at.released.igdbclient.error.IgdbApiFailureException
import at.released.igdbclient.error.IgdbException
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.error.IgdbHttpException
import at.released.igdbclient.internal.IgdbClientImplementation
import at.released.igdbclient.internal.buildRequestExecutor
import at.released.igdbclient.model.AgeRatingCategoryResult
import at.released.igdbclient.model.AgeRatingContentDescriptionResult
import at.released.igdbclient.model.AgeRatingContentDescriptionV2Result
import at.released.igdbclient.model.AgeRatingOrganizationResult
import at.released.igdbclient.model.AgeRatingResult
import at.released.igdbclient.model.AlternativeNameResult
import at.released.igdbclient.model.ArtworkResult
import at.released.igdbclient.model.CharacterGenderResult
import at.released.igdbclient.model.CharacterMugShotResult
import at.released.igdbclient.model.CharacterResult
import at.released.igdbclient.model.CharacterSpecieResult
import at.released.igdbclient.model.CollectionMembershipResult
import at.released.igdbclient.model.CollectionMembershipTypeResult
import at.released.igdbclient.model.CollectionRelationResult
import at.released.igdbclient.model.CollectionRelationTypeResult
import at.released.igdbclient.model.CollectionResult
import at.released.igdbclient.model.CollectionTypeResult
import at.released.igdbclient.model.CompanyLogoResult
import at.released.igdbclient.model.CompanyResult
import at.released.igdbclient.model.CompanyStatusResult
import at.released.igdbclient.model.CompanyWebsiteResult
import at.released.igdbclient.model.CoverResult
import at.released.igdbclient.model.DateFormatResult
import at.released.igdbclient.model.EventLogoResult
import at.released.igdbclient.model.EventNetworkResult
import at.released.igdbclient.model.EventResult
import at.released.igdbclient.model.ExternalGameResult
import at.released.igdbclient.model.ExternalGameSourceResult
import at.released.igdbclient.model.FranchiseResult
import at.released.igdbclient.model.GameEngineLogoResult
import at.released.igdbclient.model.GameEngineResult
import at.released.igdbclient.model.GameLocalizationResult
import at.released.igdbclient.model.GameModeResult
import at.released.igdbclient.model.GameReleaseFormatResult
import at.released.igdbclient.model.GameResult
import at.released.igdbclient.model.GameStatusResult
import at.released.igdbclient.model.GameTimeToBeatResult
import at.released.igdbclient.model.GameTypeResult
import at.released.igdbclient.model.GameVersionFeatureResult
import at.released.igdbclient.model.GameVersionFeatureValueResult
import at.released.igdbclient.model.GameVersionResult
import at.released.igdbclient.model.GameVideoResult
import at.released.igdbclient.model.GenreResult
import at.released.igdbclient.model.InvolvedCompanyResult
import at.released.igdbclient.model.KeywordResult
import at.released.igdbclient.model.LanguageResult
import at.released.igdbclient.model.LanguageSupportResult
import at.released.igdbclient.model.LanguageSupportTypeResult
import at.released.igdbclient.model.MultiplayerModeResult
import at.released.igdbclient.model.NetworkTypeResult
import at.released.igdbclient.model.PlatformFamilyResult
import at.released.igdbclient.model.PlatformLogoResult
import at.released.igdbclient.model.PlatformResult
import at.released.igdbclient.model.PlatformTypeResult
import at.released.igdbclient.model.PlatformVersionCompanyResult
import at.released.igdbclient.model.PlatformVersionReleaseDateResult
import at.released.igdbclient.model.PlatformVersionResult
import at.released.igdbclient.model.PlatformWebsiteResult
import at.released.igdbclient.model.PlayerPerspectiveResult
import at.released.igdbclient.model.PopularityPrimitiveResult
import at.released.igdbclient.model.PopularityTypeResult
import at.released.igdbclient.model.RegionResult
import at.released.igdbclient.model.ReleaseDateRegionResult
import at.released.igdbclient.model.ReleaseDateResult
import at.released.igdbclient.model.ReleaseDateStatusResult
import at.released.igdbclient.model.ScreenshotResult
import at.released.igdbclient.model.SearchResult
import at.released.igdbclient.model.ThemeResult
import at.released.igdbclient.model.UnpackedMultiQueryResult
import at.released.igdbclient.model.WebsiteResult
import at.released.igdbclient.model.WebsiteTypeResult

/**
 * Creates a [IgdbClient] with the specified [block] configuration.
 *
 * Sample:
 * ```kotlin
 * val client = IgdbClient(IgdbOkhttpClient) {
 *     httpClient {
 *         callFactory = <okhttp call.factory>
 *     }
 *
 *     twitchAuth {
 *         clientId = <Twitch OAuth app Client ID>
 *         clientSecret = <Twitch OAuth app Client Secret>
 *     }
 * }
 * ```
 */
public fun <C : IgdbHttpEngineConfig> IgdbClient(
    engine: IgdbHttpEngine<C>,
    block: IgdbClientConfigBlock<C>.() -> Unit,
): IgdbClient {
    val config = IgdbClientConfigBlock<C>().apply(block).build()
    val httpClient = engine.create(config)
    val executor = buildRequestExecutor(config, httpClient)
    return IgdbClientImplementation(executor)
}

/**
 * IGDB Client which allows you to call the [IGDB API](https://api-docs.igdb.com/#endpoints) methods.
 */
public interface IgdbClient {
    public val webhookApi: IgdbWebhookApi
    public val dumpApi: IgdbDumpApi
        get() = error("Not implemented")

    /**
     * General method for making requests to the igdb server.
     *
     * For the given [endpoint] and [query], executes a network request and returns the result or error
     * as an [IgdbResult] object
     *
     */
    public suspend fun <T : Any> execute(
        endpoint: IgdbEndpoint<T>,
        query: ApicalypseQuery,
    ): IgdbResult<T, IgdbHttpErrorResponse>
}

/**
 * Age Rating according to various rating organisations
 */
public suspend fun IgdbClient.getAgeRatings(builder: ApicalypseQueryBuilder.() -> Unit): AgeRatingResult =
    executeOrThrow(IgdbEndpoint.AGE_RATING, apicalypseQuery(builder))

/**
 * Age Rating according to various rating organisations
 */
public suspend fun IgdbClient.getAgeRatingCategories(
    builder: ApicalypseQueryBuilder.() -> Unit,
): AgeRatingCategoryResult = executeOrThrow(IgdbEndpoint.AGE_RATING_CATEGORY, apicalypseQuery(builder))

/**
 * Age Rating Descriptors
 */
@Deprecated("Use getAgeRatingContentDescriptionsv2 instead")
public suspend fun IgdbClient.getAgeRatingContentDescriptions(
    builder: ApicalypseQueryBuilder.() -> Unit,
): AgeRatingContentDescriptionResult {
    @Suppress("DEPRECATION")
    return executeOrThrow(IgdbEndpoint.AGE_RATING_CONTENT_DESCRIPTION, apicalypseQuery(builder))
}

/**
 * Age Rating Descriptors
 */
public suspend fun IgdbClient.getAgeRatingContentDescriptionsV2(
    builder: ApicalypseQueryBuilder.() -> Unit,
): AgeRatingContentDescriptionV2Result =
    executeOrThrow(IgdbEndpoint.AGE_RATING_CONTENT_DESCRIPTION_V2, apicalypseQuery(builder))

/**
 * Age Rating organizations
 */
public suspend fun IgdbClient.getAgeRatingOrganizations(
    builder: ApicalypseQueryBuilder.() -> Unit,
): AgeRatingOrganizationResult = executeOrThrow(IgdbEndpoint.AGE_RATING_ORGANIZATION, apicalypseQuery(builder))

/**
 * Alternative and international game titles
 */
public suspend fun IgdbClient.getAlternativeNames(builder: ApicalypseQueryBuilder.() -> Unit): AlternativeNameResult =
    executeOrThrow(IgdbEndpoint.ALTERNATIVE_NAME, apicalypseQuery(builder))

/**
 * Official artworks (resolution and aspect ratio may vary)
 */
public suspend fun IgdbClient.getArtworks(builder: ApicalypseQueryBuilder.() -> Unit): ArtworkResult =
    executeOrThrow(IgdbEndpoint.ARTWORK, apicalypseQuery(builder))

/**
 * Video game characters
 */
public suspend fun IgdbClient.getCharacters(builder: ApicalypseQueryBuilder.() -> Unit): CharacterResult =
    executeOrThrow(IgdbEndpoint.CHARACTER, apicalypseQuery(builder))

/**
 * Character gender
 */
public suspend fun IgdbClient.getCharacterGender(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CharacterGenderResult = executeOrThrow(IgdbEndpoint.CHARACTER_GENDER, apicalypseQuery(builder))

/**
 * Images depicting game characters
 */
public suspend fun IgdbClient.getCharacterMugShots(builder: ApicalypseQueryBuilder.() -> Unit): CharacterMugShotResult =
    executeOrThrow(IgdbEndpoint.CHARACTER_MUG_SHOT, apicalypseQuery(builder))

/**
 * Character species
 */
public suspend fun IgdbClient.getCharacterSpecies(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CharacterSpecieResult = executeOrThrow(IgdbEndpoint.CHARACTER_SPECIE, apicalypseQuery(builder))

/**
 * Collection, AKA Series
 */
public suspend fun IgdbClient.getCollections(builder: ApicalypseQueryBuilder.() -> Unit): CollectionResult =
    executeOrThrow(IgdbEndpoint.COLLECTION, apicalypseQuery(builder))

/**
 * Collection memberships
 */
public suspend fun IgdbClient.getCollectionMemberships(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CollectionMembershipResult = executeOrThrow(IgdbEndpoint.COLLECTION_MEMBERSHIP, apicalypseQuery(builder))

/**
 * Collection membership types
 */
public suspend fun IgdbClient.getCollectionMembershipTypes(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CollectionMembershipTypeResult = executeOrThrow(IgdbEndpoint.COLLECTION_MEMBERSHIP_TYPE, apicalypseQuery(builder))

/**
 * Describes Relationship between Collections
 */
public suspend fun IgdbClient.getCollectionRelations(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CollectionRelationResult = executeOrThrow(IgdbEndpoint.COLLECTION_RELATION, apicalypseQuery(builder))

/**
 * Collection Relation types
 */
public suspend fun IgdbClient.getCollectionRelationTypes(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CollectionRelationTypeResult = executeOrThrow(IgdbEndpoint.COLLECTION_RELATION_TYPE, apicalypseQuery(builder))

/**
 * Collection types
 */
public suspend fun IgdbClient.getCollectionTypes(builder: ApicalypseQueryBuilder.() -> Unit): CollectionTypeResult =
    executeOrThrow(IgdbEndpoint.COLLECTION_TYPE, apicalypseQuery(builder))

/**
 * Video game companies. Both publishers & developers
 */
public suspend fun IgdbClient.getCompanies(builder: ApicalypseQueryBuilder.() -> Unit): CompanyResult =
    executeOrThrow(IgdbEndpoint.COMPANY, apicalypseQuery(builder))

/**
 * The logos of developers and publishers
 */
public suspend fun IgdbClient.getCompanyLogos(builder: ApicalypseQueryBuilder.() -> Unit): CompanyLogoResult =
    executeOrThrow(IgdbEndpoint.COMPANY_LOGO, apicalypseQuery(builder))

/**
 * Company statuses
 */
public suspend fun IgdbClient.getCompanyStatuses(
    builder: ApicalypseQueryBuilder.() -> Unit,
): CompanyStatusResult = executeOrThrow(IgdbEndpoint.COMPANY_STATUS, apicalypseQuery(builder))

/**
 * Company Website
 */
public suspend fun IgdbClient.getCompanyWebsites(builder: ApicalypseQueryBuilder.() -> Unit): CompanyWebsiteResult =
    executeOrThrow(IgdbEndpoint.COMPANY_WEBSITE, apicalypseQuery(builder))

/**
 * The cover art of games
 */
public suspend fun IgdbClient.getCovers(builder: ApicalypseQueryBuilder.() -> Unit): CoverResult =
    executeOrThrow(IgdbEndpoint.COVER, apicalypseQuery(builder))

/**
 * Date formats
 */
public suspend fun IgdbClient.getDateFormats(builder: ApicalypseQueryBuilder.() -> Unit): DateFormatResult =
    executeOrThrow(IgdbEndpoint.DATE_FORMAT, apicalypseQuery(builder))

/**
 * Gaming events
 */
public suspend fun IgdbClient.getEvents(builder: ApicalypseQueryBuilder.() -> Unit): EventResult =
    executeOrThrow(IgdbEndpoint.EVENT, apicalypseQuery(builder))

/**
 * The logos of gaming events
 */
public suspend fun IgdbClient.getEventLogos(builder: ApicalypseQueryBuilder.() -> Unit): EventLogoResult =
    executeOrThrow(IgdbEndpoint.EVENT_LOGO, apicalypseQuery(builder))

/**
 * Urls related to the gaming event
 */
public suspend fun IgdbClient.getEventNetworks(builder: ApicalypseQueryBuilder.() -> Unit): EventNetworkResult =
    executeOrThrow(IgdbEndpoint.EVENT_NETWORK, apicalypseQuery(builder))

/**
 * Game IDs on other services
 */
public suspend fun IgdbClient.getExternalGames(builder: ApicalypseQueryBuilder.() -> Unit): ExternalGameResult =
    executeOrThrow(IgdbEndpoint.EXTERNAL_GAME, apicalypseQuery(builder))

/**
 * Sources for the external games
 */
public suspend fun IgdbClient.getExternalGameSources(
    builder: ApicalypseQueryBuilder.() -> Unit,
): ExternalGameSourceResult = executeOrThrow(IgdbEndpoint.EXTERNAL_GAME_SOURCE, apicalypseQuery(builder))

/**
 * A list of video game franchises such as Star Wars.
 */
public suspend fun IgdbClient.getFranchises(builder: ApicalypseQueryBuilder.() -> Unit): FranchiseResult =
    executeOrThrow(IgdbEndpoint.FRANCHISE, apicalypseQuery(builder))

/**
 * Video Games!
 */
public suspend fun IgdbClient.getGames(builder: ApicalypseQueryBuilder.() -> Unit): GameResult =
    executeOrThrow(IgdbEndpoint.GAME, apicalypseQuery(builder))

/**
 * Video game engines such as unreal engine.
 */
public suspend fun IgdbClient.getGameEngines(builder: ApicalypseQueryBuilder.() -> Unit): GameEngineResult =
    executeOrThrow(IgdbEndpoint.GAME_ENGINE, apicalypseQuery(builder))

/**
 * The logos of game engines
 */
public suspend fun IgdbClient.getGameEngineLogos(builder: ApicalypseQueryBuilder.() -> Unit): GameEngineLogoResult =
    executeOrThrow(IgdbEndpoint.GAME_ENGINE_LOGO, apicalypseQuery(builder))

/**
 * Game localization for a game
 */
public suspend fun IgdbClient.getGameLocalizations(builder: ApicalypseQueryBuilder.() -> Unit): GameLocalizationResult =
    executeOrThrow(IgdbEndpoint.GAME_LOCALIZATION, apicalypseQuery(builder))

/**
 * Single player, Multiplayer etc
 */
public suspend fun IgdbClient.getGameModes(builder: ApicalypseQueryBuilder.() -> Unit): GameModeResult =
    executeOrThrow(IgdbEndpoint.GAME_MODE, apicalypseQuery(builder))

/**
 * The format of the game release
 */
public suspend fun IgdbClient.getGameReleaseFormats(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameReleaseFormatResult = executeOrThrow(IgdbEndpoint.GAME_RELEASE_FORMAT, apicalypseQuery(builder))

/**
 * The format of the game release
 */
public suspend fun IgdbClient.getGameStatuses(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameStatusResult = executeOrThrow(IgdbEndpoint.GAME_STATUS, apicalypseQuery(builder))

/**
 * Average time to beat times for a game
 */
public suspend fun IgdbClient.getGameTimeToBeat(builder: ApicalypseQueryBuilder.() -> Unit): GameTimeToBeatResult =
    executeOrThrow(IgdbEndpoint.GAME_TIME_TO_BEAT, apicalypseQuery(builder))

/**
 * The format of the game release
 */
public suspend fun IgdbClient.getGameTypes(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameTypeResult = executeOrThrow(IgdbEndpoint.GAME_TYPE, apicalypseQuery(builder))

/**
 * Details about game editions and versions
 */
public suspend fun IgdbClient.getGameVersions(builder: ApicalypseQueryBuilder.() -> Unit): GameVersionResult =
    executeOrThrow(IgdbEndpoint.GAME_VERSION, apicalypseQuery(builder))

/**
 * Features and descriptions of what makes each version/edition different from the main game
 */
public suspend fun IgdbClient.getGameVersionFeatures(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameVersionFeatureResult = executeOrThrow(IgdbEndpoint.GAME_VERSION_FEATURE, apicalypseQuery(builder))

/**
 * The bool/text value of the feature
 */
public suspend fun IgdbClient.getGameVersionFeatureValues(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameVersionFeatureValueResult = executeOrThrow(IgdbEndpoint.GAME_VERSION_FEATURE_VALUE, apicalypseQuery(builder))

/**
 * A video associated with a game
 */
public suspend fun IgdbClient.getGameVideos(builder: ApicalypseQueryBuilder.() -> Unit): GameVideoResult =
    executeOrThrow(IgdbEndpoint.GAME_VIDEO, apicalypseQuery(builder))

/**
 * Genres of video game
 */
public suspend fun IgdbClient.getGenres(builder: ApicalypseQueryBuilder.() -> Unit): GenreResult =
    executeOrThrow(IgdbEndpoint.GENRE, apicalypseQuery(builder))

/**
 * Involved Company
 */
public suspend fun IgdbClient.getInvolvedCompanies(builder: ApicalypseQueryBuilder.() -> Unit): InvolvedCompanyResult =
    executeOrThrow(IgdbEndpoint.INVOLVED_COMPANY, apicalypseQuery(builder))

/**
 * Keywords are words or phrases that get tagged to a game such as “world war 2” or “steampunk”
 */
public suspend fun IgdbClient.getKeywords(builder: ApicalypseQueryBuilder.() -> Unit): KeywordResult =
    executeOrThrow(IgdbEndpoint.KEYWORD, apicalypseQuery(builder))

/**
 * Languages that are used in the Language Support endpoint
 */
public suspend fun IgdbClient.getLanguages(builder: ApicalypseQueryBuilder.() -> Unit): LanguageResult =
    executeOrThrow(IgdbEndpoint.LANGUAGE, apicalypseQuery(builder))

/**
 * Games can be played with different languages for voice acting, subtitles, or the interface language.
 */
public suspend fun IgdbClient.getLanguageSupports(builder: ApicalypseQueryBuilder.() -> Unit): LanguageSupportResult =
    executeOrThrow(IgdbEndpoint.LANGUAGE_SUPPORT, apicalypseQuery(builder))

/**
 * Language Support Types contains the identifiers for the support types that Language Support uses.
 */
public suspend fun IgdbClient.getLanguageSupportTypes(
    builder: ApicalypseQueryBuilder.() -> Unit,
): LanguageSupportTypeResult = executeOrThrow(IgdbEndpoint.LANGUAGE_SUPPORT_TYPE, apicalypseQuery(builder))

/**
 * Data about the supported multiplayer types
 */
public suspend fun IgdbClient.getMultiplayerModes(builder: ApicalypseQueryBuilder.() -> Unit): MultiplayerModeResult =
    executeOrThrow(IgdbEndpoint.MULTIPLAYER_MODE, apicalypseQuery(builder))

/**
 * Allows you to execute multiple queries at once
 */
public suspend fun IgdbClient.multiquery(
    builder: ApicalypseMultiQueryBuilder.() -> Unit,
): List<UnpackedMultiQueryResult<*>> {
    return executeOrThrow(IgdbEndpoint.MULTIQUERY, apicalypseMultiQuery(builder))
}

/**
 * Social networks related to the gaming event
 */
public suspend fun IgdbClient.getNetworkTypes(builder: ApicalypseQueryBuilder.() -> Unit): NetworkTypeResult =
    executeOrThrow(IgdbEndpoint.NETWORK_TYPE, apicalypseQuery(builder))

/**
 * The hardware used to run the game or game delivery network
 */
public suspend fun IgdbClient.getPlatforms(builder: ApicalypseQueryBuilder.() -> Unit): PlatformResult =
    executeOrThrow(IgdbEndpoint.PLATFORM, apicalypseQuery(builder))

/**
 * A collection of closely related platforms
 */
public suspend fun IgdbClient.getPlatformFamilies(builder: ApicalypseQueryBuilder.() -> Unit): PlatformFamilyResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_FAMILY, apicalypseQuery(builder))

/**
 * Logo for a platform
 */
public suspend fun IgdbClient.getPlatformLogos(builder: ApicalypseQueryBuilder.() -> Unit): PlatformLogoResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_LOGO, apicalypseQuery(builder))

/**
 * Type of platform
 */
public suspend fun IgdbClient.getPlatformTypes(builder: ApicalypseQueryBuilder.() -> Unit): PlatformTypeResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_TYPE, apicalypseQuery(builder))

/**
 * Platform Version
 */
public suspend fun IgdbClient.getPlatformVersions(builder: ApicalypseQueryBuilder.() -> Unit): PlatformVersionResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_VERSION, apicalypseQuery(builder))

/**
 * A platform developer
 */
public suspend fun IgdbClient.getPlatformVersionCompanies(
    builder: ApicalypseQueryBuilder.() -> Unit,
): PlatformVersionCompanyResult = executeOrThrow(IgdbEndpoint.PLATFORM_VERSION_COMPANY, apicalypseQuery(builder))

/**
 * A handy endpoint that extends platform release dates. Used to dig deeper into release dates, platforms and
 * versions
 */
public suspend fun IgdbClient.getPlatformVersionReleaseDates(
    builder: ApicalypseQueryBuilder.() -> Unit,
): PlatformVersionReleaseDateResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_VERSION_RELEASE_DATE, apicalypseQuery(builder))

/**
 * The main website for the platform
 */
public suspend fun IgdbClient.getPlatformWebsites(builder: ApicalypseQueryBuilder.() -> Unit): PlatformWebsiteResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_WEBSITE, apicalypseQuery(builder))

/**
 * Player perspectives describe the view/perspective of the player in a video game
 */
public suspend fun IgdbClient.getPlayerPerspectives(
    builder: ApicalypseQueryBuilder.() -> Unit,
): PlayerPerspectiveResult = executeOrThrow(IgdbEndpoint.PLAYER_PERSPECTIVE, apicalypseQuery(builder))

/**
 * Popularity Primitives, this endpoint lists available primitives with their source and popularity type
 */
public suspend fun IgdbClient.getPopularityPrimitives(
    builder: ApicalypseQueryBuilder.() -> Unit,
): PopularityPrimitiveResult = executeOrThrow(IgdbEndpoint.POPULARITY_PRIMITIVE, apicalypseQuery(builder))

/**
 * This describes what type of popularity primitive or popularity indicator the popularity value is
 */
public suspend fun IgdbClient.getPopularityTypes(
    builder: ApicalypseQueryBuilder.() -> Unit,
): PopularityTypeResult = executeOrThrow(IgdbEndpoint.POPULARITY_TYPE, apicalypseQuery(builder))

/**
 * Region for game localization
 */
public suspend fun IgdbClient.getRegions(builder: ApicalypseQueryBuilder.() -> Unit): RegionResult =
    executeOrThrow(IgdbEndpoint.REGION, apicalypseQuery(builder))

/**
 * A handy endpoint that extends game release dates. Used to dig deeper into release dates, platforms and versions
 */
public suspend fun IgdbClient.getReleaseDates(builder: ApicalypseQueryBuilder.() -> Unit): ReleaseDateResult =
    executeOrThrow(IgdbEndpoint.RELEASE_DATE, apicalypseQuery(builder))

/**
 * Regions for release dates
 */
public suspend fun IgdbClient.getReleaseDateRegions(
    builder: ApicalypseQueryBuilder.() -> Unit,
): ReleaseDateRegionResult = executeOrThrow(IgdbEndpoint.RELEASE_DATE_REGION, apicalypseQuery(builder))

/**
 * An endpoint to provide definition of all the current release date statuses
 */
public suspend fun IgdbClient.getReleaseDateStatuses(
    builder: ApicalypseQueryBuilder.() -> Unit,
): ReleaseDateStatusResult = executeOrThrow(IgdbEndpoint.RELEASE_DATE_STATUS, apicalypseQuery(builder))

/**
 * Screenshots of games
 */
public suspend fun IgdbClient.getScreenshots(builder: ApicalypseQueryBuilder.() -> Unit): ScreenshotResult =
    executeOrThrow(IgdbEndpoint.SCREENSHOT, apicalypseQuery(builder))

/**
 * Search
 */
public suspend fun IgdbClient.search(builder: ApicalypseQueryBuilder.() -> Unit): SearchResult =
    executeOrThrow(IgdbEndpoint.SEARCH, apicalypseQuery(builder))

/**
 * Video game themes
 */
public suspend fun IgdbClient.getThemes(builder: ApicalypseQueryBuilder.() -> Unit): ThemeResult =
    executeOrThrow(IgdbEndpoint.THEME, apicalypseQuery(builder))

/**
 * A website url, usually associated with a game
 */
public suspend fun IgdbClient.getWebsites(builder: ApicalypseQueryBuilder.() -> Unit): WebsiteResult =
    executeOrThrow(IgdbEndpoint.WEBSITE, apicalypseQuery(builder))

/**
 * Website types
 */
public suspend fun IgdbClient.getWebsiteTypes(builder: ApicalypseQueryBuilder.() -> Unit): WebsiteTypeResult =
    executeOrThrow(IgdbEndpoint.WEBSITE_TYPE, apicalypseQuery(builder))

public suspend fun <T : Any> IgdbClient.executeOrThrow(
    endpoint: IgdbEndpoint<T>,
    query: ApicalypseQuery,
): T {
    val result = this.execute(
        endpoint = endpoint,
        query = query,
    )
    @Suppress("ThrowsCount")
    if (result is Success) {
        return result.value
    } else {
        val exception = when (result) {
            is ApiFailure -> IgdbApiFailureException(result)
            is HttpFailure -> IgdbHttpException(result)
            is UnknownHttpCodeFailure -> IgdbHttpException(result)
            is NetworkFailure -> IgdbException(result.error)
            is UnknownFailure -> IgdbException(result.error)
            else -> error("Not an error")
        }
        throw exception
    }
}
