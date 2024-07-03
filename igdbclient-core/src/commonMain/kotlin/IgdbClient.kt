/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("TooManyFunctions")

package ru.pixnews.igdbclient

import ru.pixnews.igdbclient.IgdbResult.Failure.ApiFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.NetworkFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownFailure
import ru.pixnews.igdbclient.IgdbResult.Failure.UnknownHttpCodeFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.ApicalypseMultiQueryBuilder
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQueryBuilder
import ru.pixnews.igdbclient.apicalypse.apicalypseMultiQuery
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.dsl.IgdbClientConfigBlock
import ru.pixnews.igdbclient.dsl.IgdbHttpEngineConfig
import ru.pixnews.igdbclient.error.IgdbApiFailureException
import ru.pixnews.igdbclient.error.IgdbException
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpException
import ru.pixnews.igdbclient.internal.IgdbClientImplementation
import ru.pixnews.igdbclient.internal.buildRequestExecutor
import ru.pixnews.igdbclient.model.AgeRatingContentDescriptionResult
import ru.pixnews.igdbclient.model.AgeRatingResult
import ru.pixnews.igdbclient.model.AlternativeNameResult
import ru.pixnews.igdbclient.model.ArtworkResult
import ru.pixnews.igdbclient.model.CharacterMugShotResult
import ru.pixnews.igdbclient.model.CharacterResult
import ru.pixnews.igdbclient.model.CollectionMembershipResult
import ru.pixnews.igdbclient.model.CollectionMembershipTypeResult
import ru.pixnews.igdbclient.model.CollectionRelationResult
import ru.pixnews.igdbclient.model.CollectionRelationTypeResult
import ru.pixnews.igdbclient.model.CollectionResult
import ru.pixnews.igdbclient.model.CollectionTypeResult
import ru.pixnews.igdbclient.model.CompanyLogoResult
import ru.pixnews.igdbclient.model.CompanyResult
import ru.pixnews.igdbclient.model.CompanyWebsiteResult
import ru.pixnews.igdbclient.model.CoverResult
import ru.pixnews.igdbclient.model.EventLogoResult
import ru.pixnews.igdbclient.model.EventNetworkResult
import ru.pixnews.igdbclient.model.EventResult
import ru.pixnews.igdbclient.model.ExternalGameResult
import ru.pixnews.igdbclient.model.FranchiseResult
import ru.pixnews.igdbclient.model.GameEngineLogoResult
import ru.pixnews.igdbclient.model.GameEngineResult
import ru.pixnews.igdbclient.model.GameLocalizationResult
import ru.pixnews.igdbclient.model.GameModeResult
import ru.pixnews.igdbclient.model.GameResult
import ru.pixnews.igdbclient.model.GameVersionFeatureResult
import ru.pixnews.igdbclient.model.GameVersionFeatureValueResult
import ru.pixnews.igdbclient.model.GameVersionResult
import ru.pixnews.igdbclient.model.GameVideoResult
import ru.pixnews.igdbclient.model.GenreResult
import ru.pixnews.igdbclient.model.InvolvedCompanyResult
import ru.pixnews.igdbclient.model.KeywordResult
import ru.pixnews.igdbclient.model.LanguageResult
import ru.pixnews.igdbclient.model.LanguageSupportResult
import ru.pixnews.igdbclient.model.LanguageSupportTypeResult
import ru.pixnews.igdbclient.model.MultiplayerModeResult
import ru.pixnews.igdbclient.model.NetworkTypeResult
import ru.pixnews.igdbclient.model.PlatformFamilyResult
import ru.pixnews.igdbclient.model.PlatformLogoResult
import ru.pixnews.igdbclient.model.PlatformResult
import ru.pixnews.igdbclient.model.PlatformVersionCompanyResult
import ru.pixnews.igdbclient.model.PlatformVersionReleaseDateResult
import ru.pixnews.igdbclient.model.PlatformVersionResult
import ru.pixnews.igdbclient.model.PlatformWebsiteResult
import ru.pixnews.igdbclient.model.PlayerPerspectiveResult
import ru.pixnews.igdbclient.model.PopularityPrimitiveResult
import ru.pixnews.igdbclient.model.PopularityTypeResult
import ru.pixnews.igdbclient.model.RegionResult
import ru.pixnews.igdbclient.model.ReleaseDateResult
import ru.pixnews.igdbclient.model.ReleaseDateStatusResult
import ru.pixnews.igdbclient.model.ScreenshotResult
import ru.pixnews.igdbclient.model.SearchResult
import ru.pixnews.igdbclient.model.ThemeResult
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult
import ru.pixnews.igdbclient.model.WebsiteResult

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
 * Age Rating Descriptors
 */
public suspend fun IgdbClient.getAgeRatingContentDescriptions(
    builder: ApicalypseQueryBuilder.() -> Unit,
): AgeRatingContentDescriptionResult =
    executeOrThrow(IgdbEndpoint.AGE_RATING_CONTENT_DESCRIPTION, apicalypseQuery(builder))

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
 * Images depicting game characters
 */
public suspend fun IgdbClient.getCharacterMugShots(builder: ApicalypseQueryBuilder.() -> Unit): CharacterMugShotResult =
    executeOrThrow(IgdbEndpoint.CHARACTER_MUG_SHOT, apicalypseQuery(builder))

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
 * A list of video game franchises such as Star Wars.
 */
public suspend fun IgdbClient.getFranchises(builder: ApicalypseQueryBuilder.() -> Unit): FranchiseResult =
    executeOrThrow(IgdbEndpoint.FRANCHISE, apicalypseQuery(builder))

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
 * Video Games!
 */
public suspend fun IgdbClient.getGames(builder: ApicalypseQueryBuilder.() -> Unit): GameResult =
    executeOrThrow(IgdbEndpoint.GAME, apicalypseQuery(builder))

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
 * A video associated with a game
 */
public suspend fun IgdbClient.getGameVideos(builder: ApicalypseQueryBuilder.() -> Unit): GameVideoResult =
    executeOrThrow(IgdbEndpoint.GAME_VIDEO, apicalypseQuery(builder))

/**
 * The bool/text value of the feature
 */
public suspend fun IgdbClient.getGameVersionFeatureValues(
    builder: ApicalypseQueryBuilder.() -> Unit,
): GameVersionFeatureValueResult = executeOrThrow(IgdbEndpoint.GAME_VERSION_FEATURE_VALUE, apicalypseQuery(builder))

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
 * Languages that are used in the Language Support endpoint
 */
public suspend fun IgdbClient.getLanguages(builder: ApicalypseQueryBuilder.() -> Unit): LanguageResult =
    executeOrThrow(IgdbEndpoint.LANGUAGE, apicalypseQuery(builder))

/**
 * Keywords are words or phrases that get tagged to a game such as “world war 2” or “steampunk”
 */
public suspend fun IgdbClient.getKeywords(builder: ApicalypseQueryBuilder.() -> Unit): KeywordResult =
    executeOrThrow(IgdbEndpoint.KEYWORD, apicalypseQuery(builder))

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
 * Platform Version
 */
public suspend fun IgdbClient.getPlatformVersions(builder: ApicalypseQueryBuilder.() -> Unit): PlatformVersionResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_VERSION, apicalypseQuery(builder))

/**
 * A collection of closely related platforms
 */
public suspend fun IgdbClient.getPlatformFamilies(builder: ApicalypseQueryBuilder.() -> Unit): PlatformFamilyResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_FAMILY, apicalypseQuery(builder))

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
 * Logo for a platform
 */
public suspend fun IgdbClient.getPlatformLogos(builder: ApicalypseQueryBuilder.() -> Unit): PlatformLogoResult =
    executeOrThrow(IgdbEndpoint.PLATFORM_LOGO, apicalypseQuery(builder))

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
