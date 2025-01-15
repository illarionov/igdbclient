/*
 * Copyright (c) 2024-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalOkHttpApi::class)
@file:Suppress(
    "FunctionName",
    "KDOC_NO_EMPTY_TAGS",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "MagicNumber",
    "TooManyFunctions",
)

package at.released.igdbclient.integration.tests

import at.released.igdbclient.IgdbClient
import at.released.igdbclient.IgdbEndpoint
import at.released.igdbclient.IgdbEndpoint.Companion.countEndpoint
import at.released.igdbclient.apicalypse.ApicalypseQuery
import at.released.igdbclient.apicalypse.ApicalypseQueryBuilder
import at.released.igdbclient.apicalypse.apicalypseQuery
import at.released.igdbclient.error.IgdbApiFailureException
import at.released.igdbclient.error.IgdbException
import at.released.igdbclient.error.IgdbHttpErrorResponse
import at.released.igdbclient.error.IgdbHttpException
import at.released.igdbclient.executeOrThrow
import at.released.igdbclient.getAgeRatings
import at.released.igdbclient.getGames
import at.released.igdbclient.getWebsites
import at.released.igdbclient.library.test.Fixtures
import at.released.igdbclient.library.test.IgdbClientConstants
import at.released.igdbclient.library.test.TestingLoggers
import at.released.igdbclient.library.test.jupiter.MainCoroutineExtension
import at.released.igdbclient.library.test.okhttp.mockwebserver.MockWebServerFixtures.successMockResponseBuilder
import at.released.igdbclient.library.test.okhttp.mockwebserver.start
import at.released.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout
import at.released.igdbclient.model.Game
import at.released.igdbclient.model.Platform
import at.released.igdbclient.model.UnpackedMultiQueryResult
import at.released.igdbclient.multiquery
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import mockwebserver3.SocketPolicy
import mockwebserver3.SocketPolicy.DisconnectAfterRequest
import mockwebserver3.SocketPolicy.DisconnectDuringResponseBody
import mockwebserver3.SocketPolicy.NoResponse
import okhttp3.ExperimentalOkHttpApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Base class with common tests running on different implementations of the IgdbClient
 */
abstract class BaseIgdbClientImplementationTest {
    open val logger = TestingLoggers.consoleLogger.withTag("IgdbClientImplementationTest")

    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    abstract fun createIgdbClient(baseUrl: String, authToken: String? = Fixtures.TEST_TOKEN): IgdbClient

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `Implementation should correctly parse success HTTP 200 response`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/games.pb") successMockResponseBuilder().build() else null
        }

        val response = api.getGames(createTestSuccessQuery())

        response.games shouldHaveSize 5
    }

    @Test
    fun `Implementation should send correct headers`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            successMockResponseBuilder().build()
        }

        api.getGames(createTestSuccessQuery())

        server.takeRequestWithTimeout().run {
            headers.values("Accept") shouldBe listOf("application/protobuf")
            headers.values("Client-Id") shouldBe listOf(Fixtures.TEST_CLIENT_ID)
            headers.values("Authorization") shouldBe listOf("Bearer ${Fixtures.TEST_TOKEN}")
            headers.values("User-Agent") shouldBe listOf("Test user agent")
            headers.values("Header1") shouldBe listOf("HeaderValue1")
            headers.values("Header2")
                .flatMap { it.split(",") }
                .shouldBe(listOf("HeaderValue2", "HeaderValue22"))
        }
    }

    @Test
    fun `Implementation should throw correct exception on HTTP 401, incorrect authtoken`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient(
            authToken = null,
        ) { request ->
            when {
                request.headers["Authorization"] != "Bearer ${Fixtures.TEST_TOKEN}" -> MockResponse.Builder()
                    .code(401)
                    .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
                    .body(Fixtures.MockIgdbResponseContent.authFailure)
                    .build()

                else -> successMockResponseBuilder().build()
            }
        }

        val exception: IgdbHttpException = shouldThrow {
            api.getGames(createTestSuccessQuery())
        }

        exception.code shouldBe 401
        exception.rawResponseBody shouldNotBe null
    }

    @Test
    fun `Implementation should throw correct exception on HTTP 400, syntax error`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            MockResponse.Builder()
                .code(400)
                .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
                .body(Fixtures.MockIgdbResponseContent.syntaxError)
                .build()
        }

        val exception: IgdbHttpException = shouldThrow {
            api.executeOrThrow(
                IgdbEndpoint.ALTERNATIVE_NAME,
                object : ApicalypseQuery {
                    override fun toString(): String = "search  'Diablo';f *;l 5;"
                },
            )
        }

        exception.code shouldBe 400
        exception.rawResponseBody shouldBe null
        exception.response shouldBe IgdbHttpErrorResponse(
            messages = listOf(
                IgdbHttpErrorResponse.Message(
                    status = 400,
                    title = "Syntax Error",
                    cause = """Expecting a STRING as input, surround your input with quotes starting at 'Diablo'""",
                ),
            ),
        )
    }

    @Test
    fun `Implementation should throw correct exception on HTTP 404, not Found`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient()

        val exception: IgdbHttpException = shouldThrow {
            api.getWebsites(createTestSuccessQuery())
        }

        exception.code shouldBe 404
        exception.rawResponseBody shouldBe "Not Found".encodeToByteArray()
        exception.response shouldBe null
    }

    @Test
    fun `Implementation should throw correct exception on HTTP 200, unparsable response`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            MockResponse.Builder()
                .code(200)
                .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_PROTOBUF)
                .body("Not really a protobuf")
                .build()
        }

        shouldThrow<IgdbApiFailureException> {
            api.getAgeRatings(createTestSuccessQuery())
        }
    }

    @ParameterizedTest
    @MethodSource(
        "at.released.igdbclient.integration.tests.BaseIgdbClientImplementationTest#networkErrorSocketPolicies",
    )
    open fun `Implementation should throw correct exception on network error`(
        policy: SocketPolicy,
    ) = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            successMockResponseBuilder().socketPolicy(policy).build()
        }

        shouldThrowExactly<IgdbException> {
            api.getGames(createTestSuccessQuery())
        }
    }

    @Test
    fun `Implementation should correctly parse multiquery responses`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/multiquery.pb") {
                successMockResponseBuilder()
                    .body(Fixtures.MockIgdbResponseContent.multiQueryPlatformsCountPsGames)
                    .build()
            } else {
                null
            }
        }

        val response = api.multiquery {
            query(IgdbEndpoint.PLATFORM.countEndpoint(), "Count of Platforms") {}
            query(IgdbEndpoint.GAME, "Playstation Games") {
                fields("name", "category", "platforms.name")
                where("platforms !=n ")
                limit(2)
            }
        }

        response shouldHaveSize 2
        response[0] shouldBe UnpackedMultiQueryResult<Any>(
            name = "Count of Platforms",
            count = 200,
            results = null,
        )
        response[1] shouldBe UnpackedMultiQueryResult<Any>(
            name = "Playstation Games",
            count = 0,
            results = listOf(
                Game(
                    id = 176032,
                    name = "Nick Quest",
                    platforms = listOf(
                        Platform(id = 6, name = "PC (Microsoft Windows)"),
                    ),
                ),
                Game(
                    id = 50975,
                    name = "Storybook Workshop",
                    platforms = listOf(
                        Platform(id = 5, name = "Wii"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `Implementation should correctly parse count() responses`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/games/count.pb") {
                successMockResponseBuilder()
                    .body(Fixtures.MockIgdbResponseContent.countGames)
                    .build()
            } else {
                null
            }
        }

        val response = api.executeOrThrow(
            IgdbEndpoint.GAME.countEndpoint(),
            apicalypseQuery {
                fields("*")
                search("Diablo")
            },
        )

        response.count shouldBe 34
    }

    private fun startMockServerCreateClient(
        authToken: String? = Fixtures.TEST_TOKEN,
        response: (RecordedRequest) -> MockResponse? = { null },
    ): IgdbClient {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createIgdbClient(url, authToken)
    }

    companion object {
        fun createTestSuccessQuery(): ApicalypseQueryBuilder.() -> Unit = {
            search("Diablo")
            fields("*")
            limit(5)
        }

        @JvmStatic
        fun networkErrorSocketPolicies(): List<SocketPolicy> = listOf(
            DisconnectAfterRequest,
            DisconnectDuringResponseBody,
            NoResponse,
        )
    }
}
