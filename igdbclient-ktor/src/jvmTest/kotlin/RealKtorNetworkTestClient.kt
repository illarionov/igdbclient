/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.ktor

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.IgdbEndpoint
import at.released.igdbclient.IgdbEndpoint.Companion.countEndpoint
import at.released.igdbclient.apicalypse.apicalypseQuery
import at.released.igdbclient.auth.twitch.InMemoryTwitchTokenStorage
import at.released.igdbclient.auth.twitch.TwitchTokenPayload
import at.released.igdbclient.dsl.field.field
import at.released.igdbclient.executeOrThrow
import at.released.igdbclient.getCollectionMembershipTypes
import at.released.igdbclient.getCollectionMemberships
import at.released.igdbclient.getCollectionRelationTypes
import at.released.igdbclient.getCollectionRelations
import at.released.igdbclient.getCollectionTypes
import at.released.igdbclient.getEventLogos
import at.released.igdbclient.getEventNetworks
import at.released.igdbclient.getEvents
import at.released.igdbclient.getGameTimeToBeat
import at.released.igdbclient.getGames
import at.released.igdbclient.getNetworkTypes
import at.released.igdbclient.getPopularityPrimitives
import at.released.igdbclient.getPopularityTypes
import at.released.igdbclient.ktor.integration.IgdbKtorLogger
import at.released.igdbclient.library.test.TestingLoggers
import at.released.igdbclient.library.test.jupiter.MainCoroutineExtension
import at.released.igdbclient.model.CollectionMembership
import at.released.igdbclient.model.CollectionMembershipType
import at.released.igdbclient.model.CollectionRelation
import at.released.igdbclient.model.CollectionType
import at.released.igdbclient.model.Event
import at.released.igdbclient.model.EventLogo
import at.released.igdbclient.model.EventNetwork
import at.released.igdbclient.model.Game
import at.released.igdbclient.model.NetworkType
import at.released.igdbclient.model.PopularityPrimitive
import at.released.igdbclient.model.PopularityType
import at.released.igdbclient.multiquery
import at.released.igdbclient.scheme.field.PopularityPrimitiveField
import io.kotest.matchers.collections.shouldHaveSize
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

@EnabledIfEnvironmentVariable(named = "MANUAL", matches = ".+", disabledReason = "Only for manual execution")
class RealKtorNetworkTestClient {
    val logger = TestingLoggers.consoleLogger.withTag("RealNetworkTestClient")

    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val testTokenProperties = TestTokenProperties.loadFromResources()
    private val ktorHttpClient: HttpClient = HttpClient(Java) {
        install(Logging) {
            logger = IgdbKtorLogger()
            level = LogLevel.ALL
        }
    }
    private val client = IgdbClient(IgdbKtorEngine) {
        userAgent = "igdbclient/1.0.0-alpha1"

        httpClient {
            this.httpClient = ktorHttpClient
        }

        twitchAuth {
            clientId = testTokenProperties.clientId!!
            clientSecret = testTokenProperties.clientSecret!!
            storage = InMemoryTwitchTokenStorage(
                token = TwitchTokenPayload(testTokenProperties.token!!),
            )
        }

        headers {
            set("Header1", "testHeader")
            append("Header2", "testHeader2")
        }

        retryPolicy {
            maxRequestRetries = 100
            initialDelay = 10.seconds
        }
    }

    @Test
    fun executeRequest() = runBlocking {
        val response = client.getGames {
            search("Diablo")
            fields("id")
            limit(2)
        }
        logger.i { "responses: $response" }

        response.games shouldHaveSize 2

        val response2 = client.getGames {
            search("War cRaft")
            fields("id")
            limit(1)
        }

        response2.games shouldHaveSize 1

        Unit
    }

    @Test
    fun executeManySimultaneousRequests() = runBlocking {
        val responses = (1..40).map {
            async {
                client.getGames {
                    search("Diablo $it")
                    fields(Game.field.all)
                    limit(1)
                }
            }
        }.awaitAll()

        logger.i { "responses: $responses" }
    }

    @Test
    fun testMultiQuery() = runBlocking {
        val response = client.multiquery {
            query(IgdbEndpoint.PLATFORM.countEndpoint(), "Count of Platforms") {}
            query(IgdbEndpoint.GAME, "Playstation Games") {
                fields(Game.field.id, Game.field.name, Game.field.genres.name)
                where("platforms !=n ")
                limit(5)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val responseGames: List<Game>? = response[1].results as List<Game>?

        logger.i { "response2: $responseGames" }
    }

    @Test
    fun testCountResponse() = runBlocking {
        val diabloGamesCount = client.executeOrThrow(
            IgdbEndpoint.GAME.countEndpoint(),
            apicalypseQuery {
                fields("*")
                search("Diablo")
            },
        )

        logger.i { "games count: $diabloGamesCount" }
    }

    @Test
    fun testEventRequests() = runBlocking {
        val events = client.getEvents {
            fields(Event.field.all)
            limit(10)
        }
        logger.i { "events: $events" }

        val eventLogos = client.getEventLogos {
            fields(EventLogo.field.all, EventLogo.field.event.name)
            limit(10)
        }
        logger.i { "event logos: $eventLogos" }

        val eventNetworks = client.getEventNetworks {
            fields(EventNetwork.field.all)
            limit(10)
        }
        logger.i { "event networks : $eventNetworks" }

        val eventNetworksTypes = client.getNetworkTypes {
            fields(NetworkType.field.all)
            limit(100)
        }
        logger.i { "event network types : $eventNetworksTypes" }

        Unit
    }

    @Test
    fun testCollectionTypes() = runBlocking {
        val collectionMemberships = client.getCollectionMemberships {
            fields(CollectionMembership.field.all)
            limit(10)
        }
        logger.i { "collection memberships: $collectionMemberships" }

        val collectionMembershipTypes = client.getCollectionMembershipTypes {
            fields(CollectionMembershipType.field.all)
            limit(10)
        }
        logger.i { "collection membership types: $collectionMembershipTypes" }

        val collectionRelations = client.getCollectionRelations {
            fields(CollectionRelation.field.all)
            limit(10)
        }
        logger.i { "collection relations: $collectionRelations" }

        val collectionRelationTypes = client.getCollectionRelationTypes {
            fields(CollectionMembershipType.field.all)
            limit(10)
        }
        logger.i { "collection relation types: $collectionRelationTypes" }

        val collectionTypes = client.getCollectionTypes {
            fields(CollectionType.field.all)
            limit(10)
        }
        logger.i { "collection types: $collectionTypes" }

        Unit
    }

    @Test
    fun testPopularityPrimitives() = runBlocking {
        val popularityPrimitives = client.getPopularityPrimitives {
            fields(PopularityPrimitive.field.all)
            where("${PopularityPrimitiveField.GAME_ID} = 133236")
        }
        logger.i { "popularity primitives: $popularityPrimitives" }

        val popularityTypes = client.getPopularityTypes {
            fields(PopularityType.field.all)
        }
        logger.i { "popularity types: $popularityTypes" }
    }

    @Test
    fun testGameTimeToBeat() = runBlocking {
        val timeToBeat = client.getGameTimeToBeat {
            fields("*")
            where("${PopularityPrimitiveField.GAME_ID} = 133236")
        }
        logger.i { "timeToBeat: $timeToBeat" }
    }

    class TestTokenProperties(
        val clientId: String?,
        val clientSecret: String?,
        val token: String?,
    ) {
        companion object {
            private const val TEST_TOKEN_PROPERTIES_FILES = "/test_token.properties"
            fun loadFromResources(): TestTokenProperties {
                val stream = TestTokenProperties::class.java.getResourceAsStream(TEST_TOKEN_PROPERTIES_FILES)
                    ?: error("No resource `$TEST_TOKEN_PROPERTIES_FILES`")
                return stream.use {
                    val properties = Properties().apply {
                        load(stream)
                    }
                    TestTokenProperties(
                        clientId = properties.getProperty("client_id"),
                        clientSecret = properties.getProperty("client_secret"),
                        token = properties.getProperty("token"),
                    )
                }
            }
        }
    }
}
