/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient

import okio.BufferedSource
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.internal.parser.MultiQueryArrayParser
import ru.pixnews.igdbclient.model.AgeRating
import ru.pixnews.igdbclient.model.AgeRatingContentDescription
import ru.pixnews.igdbclient.model.AgeRatingContentDescriptionResult
import ru.pixnews.igdbclient.model.AgeRatingResult
import ru.pixnews.igdbclient.model.AlternativeName
import ru.pixnews.igdbclient.model.AlternativeNameResult
import ru.pixnews.igdbclient.model.Artwork
import ru.pixnews.igdbclient.model.ArtworkResult
import ru.pixnews.igdbclient.model.Character
import ru.pixnews.igdbclient.model.CharacterMugShot
import ru.pixnews.igdbclient.model.CharacterMugShotResult
import ru.pixnews.igdbclient.model.CharacterResult
import ru.pixnews.igdbclient.model.Collection
import ru.pixnews.igdbclient.model.CollectionMembership
import ru.pixnews.igdbclient.model.CollectionMembershipResult
import ru.pixnews.igdbclient.model.CollectionMembershipType
import ru.pixnews.igdbclient.model.CollectionMembershipTypeResult
import ru.pixnews.igdbclient.model.CollectionRelation
import ru.pixnews.igdbclient.model.CollectionRelationResult
import ru.pixnews.igdbclient.model.CollectionRelationType
import ru.pixnews.igdbclient.model.CollectionRelationTypeResult
import ru.pixnews.igdbclient.model.CollectionResult
import ru.pixnews.igdbclient.model.CollectionType
import ru.pixnews.igdbclient.model.CollectionTypeResult
import ru.pixnews.igdbclient.model.Company
import ru.pixnews.igdbclient.model.CompanyLogo
import ru.pixnews.igdbclient.model.CompanyLogoResult
import ru.pixnews.igdbclient.model.CompanyResult
import ru.pixnews.igdbclient.model.CompanyWebsite
import ru.pixnews.igdbclient.model.CompanyWebsiteResult
import ru.pixnews.igdbclient.model.Count
import ru.pixnews.igdbclient.model.Cover
import ru.pixnews.igdbclient.model.CoverResult
import ru.pixnews.igdbclient.model.Event
import ru.pixnews.igdbclient.model.EventLogo
import ru.pixnews.igdbclient.model.EventLogoResult
import ru.pixnews.igdbclient.model.EventNetwork
import ru.pixnews.igdbclient.model.EventNetworkResult
import ru.pixnews.igdbclient.model.EventResult
import ru.pixnews.igdbclient.model.ExternalGame
import ru.pixnews.igdbclient.model.ExternalGameResult
import ru.pixnews.igdbclient.model.Franchise
import ru.pixnews.igdbclient.model.FranchiseResult
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.GameEngine
import ru.pixnews.igdbclient.model.GameEngineLogo
import ru.pixnews.igdbclient.model.GameEngineLogoResult
import ru.pixnews.igdbclient.model.GameEngineResult
import ru.pixnews.igdbclient.model.GameLocalization
import ru.pixnews.igdbclient.model.GameLocalizationResult
import ru.pixnews.igdbclient.model.GameMode
import ru.pixnews.igdbclient.model.GameModeResult
import ru.pixnews.igdbclient.model.GameResult
import ru.pixnews.igdbclient.model.GameTimeToBeat
import ru.pixnews.igdbclient.model.GameTimeToBeatResult
import ru.pixnews.igdbclient.model.GameVersion
import ru.pixnews.igdbclient.model.GameVersionFeature
import ru.pixnews.igdbclient.model.GameVersionFeatureResult
import ru.pixnews.igdbclient.model.GameVersionFeatureValue
import ru.pixnews.igdbclient.model.GameVersionFeatureValueResult
import ru.pixnews.igdbclient.model.GameVersionResult
import ru.pixnews.igdbclient.model.GameVideo
import ru.pixnews.igdbclient.model.GameVideoResult
import ru.pixnews.igdbclient.model.Genre
import ru.pixnews.igdbclient.model.GenreResult
import ru.pixnews.igdbclient.model.IgdbWebhookId
import ru.pixnews.igdbclient.model.InvolvedCompany
import ru.pixnews.igdbclient.model.InvolvedCompanyResult
import ru.pixnews.igdbclient.model.Keyword
import ru.pixnews.igdbclient.model.KeywordResult
import ru.pixnews.igdbclient.model.Language
import ru.pixnews.igdbclient.model.LanguageResult
import ru.pixnews.igdbclient.model.LanguageSupport
import ru.pixnews.igdbclient.model.LanguageSupportResult
import ru.pixnews.igdbclient.model.LanguageSupportType
import ru.pixnews.igdbclient.model.LanguageSupportTypeResult
import ru.pixnews.igdbclient.model.MultiplayerMode
import ru.pixnews.igdbclient.model.MultiplayerModeResult
import ru.pixnews.igdbclient.model.NetworkType
import ru.pixnews.igdbclient.model.NetworkTypeResult
import ru.pixnews.igdbclient.model.Platform
import ru.pixnews.igdbclient.model.PlatformFamily
import ru.pixnews.igdbclient.model.PlatformFamilyResult
import ru.pixnews.igdbclient.model.PlatformLogo
import ru.pixnews.igdbclient.model.PlatformLogoResult
import ru.pixnews.igdbclient.model.PlatformResult
import ru.pixnews.igdbclient.model.PlatformVersion
import ru.pixnews.igdbclient.model.PlatformVersionCompany
import ru.pixnews.igdbclient.model.PlatformVersionCompanyResult
import ru.pixnews.igdbclient.model.PlatformVersionReleaseDate
import ru.pixnews.igdbclient.model.PlatformVersionReleaseDateResult
import ru.pixnews.igdbclient.model.PlatformVersionResult
import ru.pixnews.igdbclient.model.PlatformWebsite
import ru.pixnews.igdbclient.model.PlatformWebsiteResult
import ru.pixnews.igdbclient.model.PlayerPerspective
import ru.pixnews.igdbclient.model.PlayerPerspectiveResult
import ru.pixnews.igdbclient.model.PopularityPrimitive
import ru.pixnews.igdbclient.model.PopularityPrimitiveResult
import ru.pixnews.igdbclient.model.PopularityType
import ru.pixnews.igdbclient.model.PopularityTypeResult
import ru.pixnews.igdbclient.model.Region
import ru.pixnews.igdbclient.model.RegionResult
import ru.pixnews.igdbclient.model.ReleaseDate
import ru.pixnews.igdbclient.model.ReleaseDateResult
import ru.pixnews.igdbclient.model.ReleaseDateStatus
import ru.pixnews.igdbclient.model.ReleaseDateStatusResult
import ru.pixnews.igdbclient.model.Screenshot
import ru.pixnews.igdbclient.model.ScreenshotResult
import ru.pixnews.igdbclient.model.Search
import ru.pixnews.igdbclient.model.SearchResult
import ru.pixnews.igdbclient.model.Theme
import ru.pixnews.igdbclient.model.ThemeResult
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult
import ru.pixnews.igdbclient.model.Website
import ru.pixnews.igdbclient.model.WebsiteResult

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
