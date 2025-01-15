/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient

import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.internal.parser.MultiQueryArrayParser
import at.released.igdbclient.model.AgeRating
import at.released.igdbclient.model.AgeRatingContentDescription
import at.released.igdbclient.model.AgeRatingContentDescriptionResult
import at.released.igdbclient.model.AgeRatingResult
import at.released.igdbclient.model.AlternativeName
import at.released.igdbclient.model.AlternativeNameResult
import at.released.igdbclient.model.Artwork
import at.released.igdbclient.model.ArtworkResult
import at.released.igdbclient.model.Character
import at.released.igdbclient.model.CharacterMugShot
import at.released.igdbclient.model.CharacterMugShotResult
import at.released.igdbclient.model.CharacterResult
import at.released.igdbclient.model.Collection
import at.released.igdbclient.model.CollectionMembership
import at.released.igdbclient.model.CollectionMembershipResult
import at.released.igdbclient.model.CollectionMembershipType
import at.released.igdbclient.model.CollectionMembershipTypeResult
import at.released.igdbclient.model.CollectionRelation
import at.released.igdbclient.model.CollectionRelationResult
import at.released.igdbclient.model.CollectionRelationType
import at.released.igdbclient.model.CollectionRelationTypeResult
import at.released.igdbclient.model.CollectionResult
import at.released.igdbclient.model.CollectionType
import at.released.igdbclient.model.CollectionTypeResult
import at.released.igdbclient.model.Company
import at.released.igdbclient.model.CompanyLogo
import at.released.igdbclient.model.CompanyLogoResult
import at.released.igdbclient.model.CompanyResult
import at.released.igdbclient.model.CompanyWebsite
import at.released.igdbclient.model.CompanyWebsiteResult
import at.released.igdbclient.model.Count
import at.released.igdbclient.model.Cover
import at.released.igdbclient.model.CoverResult
import at.released.igdbclient.model.Event
import at.released.igdbclient.model.EventLogo
import at.released.igdbclient.model.EventLogoResult
import at.released.igdbclient.model.EventNetwork
import at.released.igdbclient.model.EventNetworkResult
import at.released.igdbclient.model.EventResult
import at.released.igdbclient.model.ExternalGame
import at.released.igdbclient.model.ExternalGameResult
import at.released.igdbclient.model.Franchise
import at.released.igdbclient.model.FranchiseResult
import at.released.igdbclient.model.Game
import at.released.igdbclient.model.GameEngine
import at.released.igdbclient.model.GameEngineLogo
import at.released.igdbclient.model.GameEngineLogoResult
import at.released.igdbclient.model.GameEngineResult
import at.released.igdbclient.model.GameLocalization
import at.released.igdbclient.model.GameLocalizationResult
import at.released.igdbclient.model.GameMode
import at.released.igdbclient.model.GameModeResult
import at.released.igdbclient.model.GameResult
import at.released.igdbclient.model.GameTimeToBeat
import at.released.igdbclient.model.GameTimeToBeatResult
import at.released.igdbclient.model.GameVersion
import at.released.igdbclient.model.GameVersionFeature
import at.released.igdbclient.model.GameVersionFeatureResult
import at.released.igdbclient.model.GameVersionFeatureValue
import at.released.igdbclient.model.GameVersionFeatureValueResult
import at.released.igdbclient.model.GameVersionResult
import at.released.igdbclient.model.GameVideo
import at.released.igdbclient.model.GameVideoResult
import at.released.igdbclient.model.Genre
import at.released.igdbclient.model.GenreResult
import at.released.igdbclient.model.IgdbWebhookId
import at.released.igdbclient.model.InvolvedCompany
import at.released.igdbclient.model.InvolvedCompanyResult
import at.released.igdbclient.model.Keyword
import at.released.igdbclient.model.KeywordResult
import at.released.igdbclient.model.Language
import at.released.igdbclient.model.LanguageResult
import at.released.igdbclient.model.LanguageSupport
import at.released.igdbclient.model.LanguageSupportResult
import at.released.igdbclient.model.LanguageSupportType
import at.released.igdbclient.model.LanguageSupportTypeResult
import at.released.igdbclient.model.MultiplayerMode
import at.released.igdbclient.model.MultiplayerModeResult
import at.released.igdbclient.model.NetworkType
import at.released.igdbclient.model.NetworkTypeResult
import at.released.igdbclient.model.Platform
import at.released.igdbclient.model.PlatformFamily
import at.released.igdbclient.model.PlatformFamilyResult
import at.released.igdbclient.model.PlatformLogo
import at.released.igdbclient.model.PlatformLogoResult
import at.released.igdbclient.model.PlatformResult
import at.released.igdbclient.model.PlatformVersion
import at.released.igdbclient.model.PlatformVersionCompany
import at.released.igdbclient.model.PlatformVersionCompanyResult
import at.released.igdbclient.model.PlatformVersionReleaseDate
import at.released.igdbclient.model.PlatformVersionReleaseDateResult
import at.released.igdbclient.model.PlatformVersionResult
import at.released.igdbclient.model.PlatformWebsite
import at.released.igdbclient.model.PlatformWebsiteResult
import at.released.igdbclient.model.PlayerPerspective
import at.released.igdbclient.model.PlayerPerspectiveResult
import at.released.igdbclient.model.PopularityPrimitive
import at.released.igdbclient.model.PopularityPrimitiveResult
import at.released.igdbclient.model.PopularityType
import at.released.igdbclient.model.PopularityTypeResult
import at.released.igdbclient.model.Region
import at.released.igdbclient.model.RegionResult
import at.released.igdbclient.model.ReleaseDate
import at.released.igdbclient.model.ReleaseDateResult
import at.released.igdbclient.model.ReleaseDateStatus
import at.released.igdbclient.model.ReleaseDateStatusResult
import at.released.igdbclient.model.Screenshot
import at.released.igdbclient.model.ScreenshotResult
import at.released.igdbclient.model.Search
import at.released.igdbclient.model.SearchResult
import at.released.igdbclient.model.Theme
import at.released.igdbclient.model.ThemeResult
import at.released.igdbclient.model.UnpackedMultiQueryResult
import at.released.igdbclient.model.Website
import at.released.igdbclient.model.WebsiteResult
import okio.BufferedSource

/**
 * IGDB endpoints
 *
 * https://api-docs.igdb.com/
 */
public open class IgdbEndpoint<out R : Any>(
    public val endpoint: String,
    public val resultParser: (ApicalypseQuery, BufferedSource) -> R,
    public val singleItemParser: ((BufferedSource) -> Any)? = null,
) {
    public open val protobufPath: String
        get() = "$endpoint.pb"

    public open val jsonPath: String
        get() = endpoint

    public open val webhookPath: String
        get() = "$endpoint/webhooks"

    public open fun getTestWebhookPath(webhookId: IgdbWebhookId): String = "$endpoint/webhooks/test/${webhookId.value}"

    public companion object {
        /**
         * Age Rating according to various rating organisations
         */
        public val AGE_RATING: IgdbEndpoint<AgeRatingResult> = IgdbEndpoint(
            "age_ratings",
            AgeRatingResult.ADAPTER::decode,
            AgeRating.ADAPTER::decode,
        )

        /**
         * Age Rating Descriptors
         */
        public val AGE_RATING_CONTENT_DESCRIPTION: IgdbEndpoint<AgeRatingContentDescriptionResult> =
            IgdbEndpoint(
                "age_rating_content_descriptions",
                AgeRatingContentDescriptionResult.ADAPTER::decode,
                AgeRatingContentDescription.ADAPTER::decode,
            )

        /**
         * Alternative and international game titles
         */
        public val ALTERNATIVE_NAME: IgdbEndpoint<AlternativeNameResult> = IgdbEndpoint(
            "alternative_names",
            AlternativeNameResult.ADAPTER::decode,
            AlternativeName.ADAPTER::decode,
        )

        /**
         * Official artworks (resolution and aspect ratio may vary)
         */
        public val ARTWORK: IgdbEndpoint<ArtworkResult> = IgdbEndpoint(
            "artworks",
            ArtworkResult.ADAPTER::decode,
            Artwork.ADAPTER::decode,
        )

        /**
         * Video game characters
         */
        public val CHARACTER: IgdbEndpoint<CharacterResult> = IgdbEndpoint(
            "characters",
            CharacterResult.ADAPTER::decode,
            Character.ADAPTER::decode,
        )

        /**
         * Images depicting game characters
         */
        public val CHARACTER_MUG_SHOT: IgdbEndpoint<CharacterMugShotResult> = IgdbEndpoint(
            "character_mug_shots",
            CharacterMugShotResult.ADAPTER::decode,
            CharacterMugShot.ADAPTER::decode,
        )

        /**
         * Collection, AKA Series
         */
        public val COLLECTION: IgdbEndpoint<CollectionResult> = IgdbEndpoint(
            "collections",
            CollectionResult.ADAPTER::decode,
            Collection.ADAPTER::decode,
        )

        /**
         * Collection Memberships
         */
        public val COLLECTION_MEMBERSHIP: IgdbEndpoint<CollectionMembershipResult> = IgdbEndpoint(
            "collection_memberships",
            CollectionMembershipResult.ADAPTER::decode,
            CollectionMembership.ADAPTER::decode,
        )

        /**
         * Collection Membership Types
         */
        public val COLLECTION_MEMBERSHIP_TYPE: IgdbEndpoint<CollectionMembershipTypeResult> = IgdbEndpoint(
            "collection_membership_types",
            CollectionMembershipTypeResult.ADAPTER::decode,
            CollectionMembershipType.ADAPTER::decode,
        )

        /**
         * Describes Relationship between Collections
         */
        public val COLLECTION_RELATION: IgdbEndpoint<CollectionRelationResult> = IgdbEndpoint(
            "collection_relations",
            CollectionRelationResult.ADAPTER::decode,
            CollectionRelation.ADAPTER::decode,
        )

        /**
         * Collection Relation Types
         */
        public val COLLECTION_RELATION_TYPE: IgdbEndpoint<CollectionRelationTypeResult> = IgdbEndpoint(
            "collection_relation_types",
            CollectionRelationTypeResult.ADAPTER::decode,
            CollectionRelationType.ADAPTER::decode,
        )

        /**
         * Enums for collection types.
         */
        public val COLLECTION_TYPE: IgdbEndpoint<CollectionTypeResult> = IgdbEndpoint(
            "collection_types",
            CollectionTypeResult.ADAPTER::decode,
            CollectionType.ADAPTER::decode,
        )

        /**
         * Video game companies. Both publishers & developers
         */
        public val COMPANY: IgdbEndpoint<CompanyResult> = IgdbEndpoint(
            "companies",
            CompanyResult.ADAPTER::decode,
            Company.ADAPTER::decode,
        )

        /**
         * The logos of developers and publishers
         */
        public val COMPANY_LOGO: IgdbEndpoint<CompanyLogoResult> = IgdbEndpoint(
            "company_logos",
            CompanyLogoResult.ADAPTER::decode,
            CompanyLogo.ADAPTER::decode,
        )

        /**
         * Company Website
         */
        public val COMPANY_WEBSITE: IgdbEndpoint<CompanyWebsiteResult> = IgdbEndpoint(
            "company_websites",
            CompanyWebsiteResult.ADAPTER::decode,
            CompanyWebsite.ADAPTER::decode,
        )

        /**
         * The cover art of games
         */
        public val COVER: IgdbEndpoint<CoverResult> = IgdbEndpoint(
            "covers",
            CoverResult.ADAPTER::decode,
            Cover.ADAPTER::decode,
        )

        /**
         * Gaming events
         */
        public val EVENT: IgdbEndpoint<EventResult> = IgdbEndpoint(
            "events",
            EventResult.ADAPTER::decode,
            Event.ADAPTER::decode,
        )

        /**
         * The logos of gaming events
         */
        public val EVENT_LOGO: IgdbEndpoint<EventLogoResult> = IgdbEndpoint(
            "event_logos",
            EventLogoResult.ADAPTER::decode,
            EventLogo.ADAPTER::decode,
        )

        /**
         * Urls related to the gaming event
         */
        public val EVENT_NETWORK: IgdbEndpoint<EventNetworkResult> = IgdbEndpoint(
            "event_networks",
            EventNetworkResult.ADAPTER::decode,
            EventNetwork.ADAPTER::decode,
        )

        /**
         * Game IDs on other services
         */
        public val EXTERNAL_GAME: IgdbEndpoint<ExternalGameResult> = IgdbEndpoint(
            "external_games",
            ExternalGameResult.ADAPTER::decode,
            ExternalGame.ADAPTER::decode,
        )

        /**
         * A list of video game franchises such as Star Wars.
         */
        public val FRANCHISE: IgdbEndpoint<FranchiseResult> = IgdbEndpoint(
            "franchises",
            FranchiseResult.ADAPTER::decode,
            Franchise.ADAPTER::decode,
        )

        /**
         * Video game engines such as unreal engine.
         */
        public val GAME_ENGINE: IgdbEndpoint<GameEngineResult> = IgdbEndpoint(
            "game_engines",
            GameEngineResult.ADAPTER::decode,
            GameEngine.ADAPTER::decode,
        )

        /**
         * The logos of game engines
         */
        public val GAME_ENGINE_LOGO: IgdbEndpoint<GameEngineLogoResult> = IgdbEndpoint(
            "game_engine_logos",
            GameEngineLogoResult.ADAPTER::decode,
            GameEngineLogo.ADAPTER::decode,
        )

        /**
         * Video Games!
         */
        public val GAME: IgdbEndpoint<GameResult> = IgdbEndpoint(
            "games",
            GameResult.ADAPTER::decode,
            Game.ADAPTER::decode,
        )

        /**
         * Game localization for a game
         */
        public val GAME_LOCALIZATION: IgdbEndpoint<GameLocalizationResult> = IgdbEndpoint(
            "game_localizations",
            GameLocalizationResult.ADAPTER::decode,
            GameLocalization.ADAPTER::decode,
        )

        /**
         * Single player, Multiplayer etc
         */
        public val GAME_MODE: IgdbEndpoint<GameModeResult> = IgdbEndpoint(
            "game_modes",
            GameModeResult.ADAPTER::decode,
            GameMode.ADAPTER::decode,
        )

        /**
         * Average time to beat times for a game
         */
        public val GAME_TIME_TO_BEAT: IgdbEndpoint<GameTimeToBeatResult> = IgdbEndpoint(
            "game_time_to_beats",
            GameTimeToBeatResult.ADAPTER::decode,
            GameTimeToBeat.ADAPTER::decode,
        )

        /**
         * Details about game editions and versions
         */
        public val GAME_VERSION: IgdbEndpoint<GameVersionResult> = IgdbEndpoint(
            "game_versions",
            GameVersionResult.ADAPTER::decode,
            GameVersion.ADAPTER::decode,
        )

        /**
         * Features and descriptions of what makes each version/edition different from the main game
         */
        public val GAME_VERSION_FEATURE: IgdbEndpoint<GameVersionFeatureResult> = IgdbEndpoint(
            "game_version_features",
            GameVersionFeatureResult.ADAPTER::decode,
            GameVersionFeature.ADAPTER::decode,
        )

        /**
         * A video associated with a game
         */
        public val GAME_VIDEO: IgdbEndpoint<GameVideoResult> = IgdbEndpoint(
            "game_videos",
            GameVideoResult.ADAPTER::decode,
            GameVideo.ADAPTER::decode,
        )

        /**
         * The bool/text value of the feature
         */
        public val GAME_VERSION_FEATURE_VALUE: IgdbEndpoint<GameVersionFeatureValueResult> =
            IgdbEndpoint(
                "game_version_feature_values",
                GameVersionFeatureValueResult.ADAPTER::decode,
                GameVersionFeatureValue.ADAPTER::decode,
            )

        /**
         * Genres of video game
         */
        public val GENRE: IgdbEndpoint<GenreResult> = IgdbEndpoint(
            "genres",
            GenreResult.ADAPTER::decode,
            Genre.ADAPTER::decode,
        )

        /**
         * Involved Company
         */
        public val INVOLVED_COMPANY: IgdbEndpoint<InvolvedCompanyResult> = IgdbEndpoint(
            "involved_companies",
            InvolvedCompanyResult.ADAPTER::decode,
            InvolvedCompany.ADAPTER::decode,
        )

        /**
         * Languages that are used in the Language Support endpoint
         */
        public val LANGUAGE: IgdbEndpoint<LanguageResult> = IgdbEndpoint(
            "languages",
            LanguageResult.ADAPTER::decode,
            Language.ADAPTER::decode,
        )

        /**
         * Keywords are words or phrases that get tagged to a game such as “world war 2” or “steampunk”
         */
        public val KEYWORD: IgdbEndpoint<KeywordResult> = IgdbEndpoint(
            "keywords",
            KeywordResult.ADAPTER::decode,
            Keyword.ADAPTER::decode,
        )

        /**
         * Games can be played with different languages for voice acting, subtitles, or the interface language.
         */
        public val LANGUAGE_SUPPORT: IgdbEndpoint<LanguageSupportResult> = IgdbEndpoint(
            "language_supports",
            LanguageSupportResult.ADAPTER::decode,
            LanguageSupport.ADAPTER::decode,
        )

        /**
         * Language Support Types contains the identifiers for the support types that Language Support uses.
         */
        public val LANGUAGE_SUPPORT_TYPE: IgdbEndpoint<LanguageSupportTypeResult> = IgdbEndpoint(
            "language_support_types",
            LanguageSupportTypeResult.ADAPTER::decode,
            LanguageSupportType.ADAPTER::decode,
        )

        /**
         * Data about the supported multiplayer types
         */
        public val MULTIPLAYER_MODE: IgdbEndpoint<MultiplayerModeResult> = IgdbEndpoint(
            "multiplayer_modes",
            MultiplayerModeResult.ADAPTER::decode,
            MultiplayerMode.ADAPTER::decode,
        )

        /**
         * Social networks related to the gaming event
         */
        public val NETWORK_TYPE: IgdbEndpoint<NetworkTypeResult> = IgdbEndpoint(
            "network_types",
            NetworkTypeResult.ADAPTER::decode,
            NetworkType.ADAPTER::decode,
        )

        /**
         * The hardware used to run the game or game delivery network
         */
        public val PLATFORM: IgdbEndpoint<PlatformResult> = IgdbEndpoint(
            "platforms",
            PlatformResult.ADAPTER::decode,
            Platform.ADAPTER::decode,
        )

        /**
         * Platform Version
         */
        public val PLATFORM_VERSION: IgdbEndpoint<PlatformVersionResult> = IgdbEndpoint(
            "platform_versions",
            PlatformVersionResult.ADAPTER::decode,
            PlatformVersion.ADAPTER::decode,
        )

        /**
         * A collection of closely related platforms
         */
        public val PLATFORM_FAMILY: IgdbEndpoint<PlatformFamilyResult> = IgdbEndpoint(
            "platform_families",
            PlatformFamilyResult.ADAPTER::decode,
            PlatformFamily.ADAPTER::decode,
        )

        /**
         * A platform developer
         */
        public val PLATFORM_VERSION_COMPANY: IgdbEndpoint<PlatformVersionCompanyResult> =
            IgdbEndpoint(
                "platform_version_companies",
                PlatformVersionCompanyResult.ADAPTER::decode,
                PlatformVersionCompany.ADAPTER::decode,
            )

        /**
         * A handy endpoint that extends platform release dates. Used to dig deeper into release dates, platforms and
         * versions
         */
        public val PLATFORM_VERSION_RELEASE_DATE: IgdbEndpoint<PlatformVersionReleaseDateResult> =
            IgdbEndpoint(
                "platform_version_release_dates",
                PlatformVersionReleaseDateResult.ADAPTER::decode,
                PlatformVersionReleaseDate.ADAPTER::decode,
            )

        /**
         * The main website for the platform
         */
        public val PLATFORM_WEBSITE: IgdbEndpoint<PlatformWebsiteResult> = IgdbEndpoint(
            "platform_websites",
            PlatformWebsiteResult.ADAPTER::decode,
            PlatformWebsite.ADAPTER::decode,
        )

        /**
         * Logo for a platform
         */
        public val PLATFORM_LOGO: IgdbEndpoint<PlatformLogoResult> = IgdbEndpoint(
            "platform_logos",
            PlatformLogoResult.ADAPTER::decode,
            PlatformLogo.ADAPTER::decode,
        )

        /**
         * Player perspectives describe the view/perspective of the player in a video game
         */
        public val PLAYER_PERSPECTIVE: IgdbEndpoint<PlayerPerspectiveResult> = IgdbEndpoint(
            "player_perspectives",
            PlayerPerspectiveResult.ADAPTER::decode,
            PlayerPerspective.ADAPTER::decode,
        )

        /**
         * Popularity Primitives, this endpoint lists available primitives with their source and popularity type
         */
        public val POPULARITY_PRIMITIVE: IgdbEndpoint<PopularityPrimitiveResult> = IgdbEndpoint(
            "popularity_primitives",
            PopularityPrimitiveResult.ADAPTER::decode,
            PopularityPrimitive.ADAPTER::decode,
        )

        /**
         * This describes what type of popularity primitive or popularity indicator the popularity value is
         */
        public val POPULARITY_TYPE: IgdbEndpoint<PopularityTypeResult> = IgdbEndpoint(
            "popularity_types",
            PopularityTypeResult.ADAPTER::decode,
            PopularityType.ADAPTER::decode,
        )

        /**
         * Region for game localization
         */
        public val REGION: IgdbEndpoint<RegionResult> = IgdbEndpoint(
            "regions",
            RegionResult.ADAPTER::decode,
            Region.ADAPTER::decode,
        )

        /**
         * A handy endpoint that extends game release dates. Used to dig deeper into release dates, platforms
         * and versions
         */
        public val RELEASE_DATE: IgdbEndpoint<ReleaseDateResult> = IgdbEndpoint(
            "release_dates",
            ReleaseDateResult.ADAPTER::decode,
            ReleaseDate.ADAPTER::decode,
        )

        /**
         * An endpoint to provide definition of all of the current release date statuses
         */
        public val RELEASE_DATE_STATUS: IgdbEndpoint<ReleaseDateStatusResult> = IgdbEndpoint(
            "release_date_statuses",
            ReleaseDateStatusResult.ADAPTER::decode,
            ReleaseDateStatus.ADAPTER::decode,
        )

        /**
         * Screenshots of games
         */
        public val SCREENSHOT: IgdbEndpoint<ScreenshotResult> = IgdbEndpoint(
            "screenshots",
            ScreenshotResult.ADAPTER::decode,
            Screenshot.ADAPTER::decode,
        )

        /**
         * Search
         */
        public val SEARCH: IgdbEndpoint<SearchResult> = IgdbEndpoint(
            "search",
            SearchResult.ADAPTER::decode,
            Search.ADAPTER::decode,
        )

        /**
         * Video game themes
         */
        public val THEME: IgdbEndpoint<ThemeResult> = IgdbEndpoint(
            "themes",
            ThemeResult.ADAPTER::decode,
            Theme.ADAPTER::decode,
        )

        /**
         * A website url, usually associated with a game
         */
        public val WEBSITE: IgdbEndpoint<WebsiteResult> = IgdbEndpoint(
            "websites",
            WebsiteResult.ADAPTER::decode,
            Website.ADAPTER::decode,
        )

        /**
         * Endpoint to execute multi-queries
         */
        public val MULTIQUERY: IgdbEndpoint<List<UnpackedMultiQueryResult<*>>> = IgdbEndpoint(
            "multiquery",
            MultiQueryArrayParser::parse,
            null,
        )

        public fun IgdbEndpoint<*>.countEndpoint(): IgdbEndpoint<Count> = IgdbEndpoint<Count, Nothing>(
            this.endpoint + "/count",
            Count.ADAPTER::decode,
            null,
        )

        private operator fun <LR : Any, R : Any> invoke(
            endpoint: String,
            resultParser: (BufferedSource) -> LR,
            singleItemParser: ((BufferedSource) -> R)? = null,
        ): IgdbEndpoint<LR> = IgdbEndpoint(
            endpoint = endpoint,
            resultParser = { _, stream -> resultParser(stream) },
            singleItemParser = singleItemParser,
        )
    }
}
