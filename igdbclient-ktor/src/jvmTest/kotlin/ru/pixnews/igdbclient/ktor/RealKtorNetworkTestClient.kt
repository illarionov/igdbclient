/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.ktor

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
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.countEndpoint
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.auth.twitch.InMemoryTwitchTokenStorage
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload
import ru.pixnews.igdbclient.dsl.field.field
import ru.pixnews.igdbclient.executeOrThrow
import ru.pixnews.igdbclient.getEventLogos
import ru.pixnews.igdbclient.getEventNetworkTypes
import ru.pixnews.igdbclient.getEventNetworks
import ru.pixnews.igdbclient.getEvents
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.ktor.integration.IgdbKtorLogger
import ru.pixnews.igdbclient.library.test.TestingLoggers
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.model.Event
import ru.pixnews.igdbclient.model.EventLogo
import ru.pixnews.igdbclient.model.EventNetwork
import ru.pixnews.igdbclient.model.EventNetworkType
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.multiquery
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
        developmentMode = true
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
            fields(EventLogo.field.all)
            limit(10)
        }
        logger.i { "event logos: $eventLogos" }

        val eventNetworks = client.getEventNetworks {
            fields(EventNetwork.field.all)
            limit(10)
        }
        logger.i { "event networks : $eventNetworks" }

        val eventNetworksTypes = client.getEventNetworkTypes {
            fields(EventNetworkType.field.all)
            limit(100)
        }
        logger.i { "event network types : $eventNetworksTypes" }

        Unit
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
