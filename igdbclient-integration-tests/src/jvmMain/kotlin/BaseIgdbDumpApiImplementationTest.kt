/*
 * Copyright (c) 2024, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalOkHttpApi::class)
@file:Suppress("FunctionNaming", "MagicNumber")

package ru.pixnews.igdbclient.integration.tests

import com.squareup.wire.ofEpochSecond
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.ExperimentalOkHttpApi
import okhttp3.Headers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.IgdbDumpApi
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.getDump
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.MediaType
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout
import ru.pixnews.igdbclient.model.dump.IgdbDump
import ru.pixnews.igdbclient.model.dump.IgdbDumpSummary

/**
 * Base class with common tests running on different implementations of the IgdbDumpApi
 */
abstract class BaseIgdbDumpApiImplementationTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    abstract fun createIgdbClient(url: String): IgdbClient

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun startMockServerCreateClient(
        response: (RecordedRequest) -> MockResponse? = { null },
    ): IgdbDumpApi {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createIgdbClient(url).dumpApi
    }

    companion object {
        const val DUMPS_RESPONSE: String = """
            [
              {
                     "endpoint": "games",
                     "file_name": "1234567890_games.csv",
                     "updated_at": 1234567890
              }
            ]
        """
        const val DUMP_GAMES_RESPONSE: String = """
            {
                "s3_url": "S3_DOWNLOAD_URL",
                "endpoint": "games",
                "file_name": "1234567890_games.csv",
                "size_bytes": 123456789,
                "updated_at": 1234567890,
                "schema_version": "1234567890",
                "schema": {
                    "id": "LONG",
                    "name": "STRING",
                    "url": "STRING",
                    "franchises": "LONG[]",
                    "rating": "DOUBLE",
                    "created_at": "TIMESTAMP",
                    "checksum": "UUID"
                }
            }
        """
        val DUMP_GAMES_RESPONSE_DUMP: IgdbDump = IgdbDump(
            s3Url = "S3_DOWNLOAD_URL",
            endpoint = "games",
            fileName = "1234567890_games.csv",
            sizeBytes = 123_456_789,
            updatedAt = ofEpochSecond(1_234_567_890, 0),
            schemaVersion = "1234567890",
            schema = mapOf(
                "id" to "LONG",
                "name" to "STRING",
                "url" to "STRING",
                "franchises" to "LONG[]",
                "rating" to "DOUBLE",
                "created_at" to "TIMESTAMP",
                "checksum" to "UUID",
            ),
        )

        fun createSuccessMockResponse(response: String = DUMPS_RESPONSE) = MockResponse(
            code = 200,
            headers = Headers.headersOf("Content-Type", MediaType.APPLICATION_JSON),
            body = response,
        )

        fun RecordedRequest.validateRequestParams() {
            method shouldBe "GET"
            headers.values("Accept") shouldBe listOf("application/json")
        }
    }

    @Nested
    @DisplayName("getAllDumps()")
    inner class GetAllDumpsTests {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/dumps" && request.method == "GET") createSuccessMockResponse() else null
        }

        @Test
        fun `getAllDumps() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.getDumps() as? IgdbResult.Success<List<IgdbDumpSummary>>

            checkNotNull(response).value.shouldContainOnly(
                IgdbDumpSummary(
                    endpoint = "games",
                    fileName = "1234567890_games.csv",
                    updatedAt = ofEpochSecond(1_234_567_890, 0),
                ),
            )
        }

        @Test
        fun `getAllDumps() should send correct request`() = coroutinesExt.runTest {
            api.getDumps()
            server.takeRequestWithTimeout().validateRequestParams()
        }
    }

    @Nested
    @DisplayName("getDump()")
    inner class GetDumpTests {
        val api = startMockServerCreateClient { request ->
            if (request.path == "/v4/dumps/games" && request.method == "GET") {
                createSuccessMockResponse(
                    response = DUMP_GAMES_RESPONSE,
                )
            } else {
                null
            }
        }

        @Test
        fun `getAllDump() should correctly parse success response`() = coroutinesExt.runTest {
            val response = api.getDump(endpoint = IgdbEndpoint.GAME) as? IgdbResult.Success<IgdbDump>
            checkNotNull(response).value shouldBe DUMP_GAMES_RESPONSE_DUMP
        }

        @Test
        fun `getAllDumps() should send correct request`() = coroutinesExt.runTest {
            api.getDumps()
            server.takeRequestWithTimeout().validateRequestParams()
        }
    }
}
