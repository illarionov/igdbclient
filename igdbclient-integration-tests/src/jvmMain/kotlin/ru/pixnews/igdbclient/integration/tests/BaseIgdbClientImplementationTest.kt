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
@file:Suppress(
    "FunctionName",
    "KDOC_NO_EMPTY_TAGS",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "MagicNumber",
    "TooManyFunctions",
)

package ru.pixnews.igdbclient.integration.tests

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import mockwebserver3.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbEndpoint.Companion.countEndpoint
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery
import ru.pixnews.igdbclient.apicalypse.ApicalypseQueryBuilder
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.error.IgdbApiFailureException
import ru.pixnews.igdbclient.error.IgdbException
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.error.IgdbHttpException
import ru.pixnews.igdbclient.executeOrThrow
import ru.pixnews.igdbclient.getAgeRatings
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.getWebsites
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants
import ru.pixnews.igdbclient.library.test.TestingLoggers
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.MockWebServerFixtures.createSuccessMockResponse
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.Platform
import ru.pixnews.igdbclient.model.UnpackedMultiQueryResult
import ru.pixnews.igdbclient.multiquery

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
            if (request.path == "/v4/games.pb") createSuccessMockResponse() else null
        }

        val response = api.getGames(createTestSuccessQuery())

        response.games shouldHaveSize 5
    }

    @Test
    fun `Implementation should send correct headers`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            createSuccessMockResponse()
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
                request.getHeader("Authorization") != "Bearer ${Fixtures.TEST_TOKEN}" -> MockResponse()
                    .setResponseCode(401)
                    .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
                    .setBody(Fixtures.MockIgdbResponseContent.authFailure)

                else -> createSuccessMockResponse()
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
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_JSON)
                .setBody(Fixtures.MockIgdbResponseContent.syntaxError)
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
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", IgdbClientConstants.MediaType.APPLICATION_PROTOBUF)
                .setBody("Not really a protobuf")
        }

        shouldThrow<IgdbApiFailureException> {
            api.getAgeRatings(createTestSuccessQuery())
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = SocketPolicy::class,
        names = [
            "DISCONNECT_AFTER_REQUEST",
            "DISCONNECT_DURING_RESPONSE_BODY",
            "NO_RESPONSE",
        ],
    )
    open fun `Implementation should throw correct exception on network error`(
        policy: SocketPolicy,
    ) = coroutinesExt.runTest {
        val api = startMockServerCreateClient {
            createSuccessMockResponse().setSocketPolicy(policy)
        }

        shouldThrowExactly<IgdbException> {
            api.getGames(createTestSuccessQuery())
        }
    }

    @Test
    fun `Implementation should correctly parse multiquery responses`() = coroutinesExt.runTest {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/multiquery.pb") {
                createSuccessMockResponse()
                    .setBody(Fixtures.MockIgdbResponseContent.multiQueryPlatformsCountPsGames)
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
                createSuccessMockResponse()
                    .setBody(Fixtures.MockIgdbResponseContent.countGames)
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
    }
}
