/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("FunctionName", "MISSING_KDOC_CLASS_ELEMENTS", "KDOC_NO_EMPTY_TAGS", "MISSING_KDOC_ON_FUNCTION")

package ru.pixnews.igdbclient.integration.tests

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.internal.RequestExecutor
import ru.pixnews.igdbclient.internal.model.IgdbAuthToken
import ru.pixnews.igdbclient.library.test.Fixtures
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.Header.AUTHORIZATION
import ru.pixnews.igdbclient.library.test.IgdbClientConstants.Header.CLIENT_ID
import ru.pixnews.igdbclient.library.test.jupiter.MainCoroutineExtension
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.start
import ru.pixnews.igdbclient.library.test.okhttp.mockwebserver.takeRequestWithTimeout

/**
 * Base class with integration tests running on different implementations of the RequestExecutor
 */
abstract class BaseRequestExecutorTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()
    val server: MockWebServer = MockWebServer()

    abstract fun createRequestExecutor(
        baseUrl: String,
        authToken: IgdbAuthToken? = null,
        userAgent: String? = "Test user agent",
        headers: Map<String, List<String>> = emptyMap(),
        backgroundDispatcher: CoroutineDispatcher = coroutinesExt.dispatcher,
    ): RequestExecutor

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `User headers should take precedence over AuthToken-provided headers `() = coroutinesExt.runTest {
        val testAuthToken = object : IgdbAuthToken {
            override val clientId: String = "authtoken-provided-client-id"
            override val token: String = "authtoken-provided-token"
        }

        val executor = startMockServerCreateClient(
            token = testAuthToken,
            headers = mapOf(
                CLIENT_ID to listOf(Fixtures.TEST_CLIENT_ID),
                AUTHORIZATION to listOf("Bearer ${Fixtures.TEST_TOKEN}"),
            ),
        )

        executor.invoke<Any>(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _ -> "" }),
        )

        server.takeRequestWithTimeout().run {
            headers.values("Client-Id") shouldBe listOf(Fixtures.TEST_CLIENT_ID)
            headers.values("Authorization") shouldBe listOf("Bearer ${Fixtures.TEST_TOKEN}")
        }
    }

    @Test
    fun `Executor should not encode slashes in path`() = coroutinesExt.runTest {
        val executor = startMockServerCreateClient()

        executor.invoke<Any>(
            ApicalypsePostRequest("games/count", apicalypseQuery { }, { _ -> "" }),
        )

        server.takeRequestWithTimeout().run {
            path shouldBe "/v4/games/count"
        }
    }

    private fun startMockServerCreateClient(
        token: IgdbAuthToken? = null,
        userAgent: String? = "Test user agent",
        headers: Map<String, List<String>> = emptyMap(),
        backgroundDispatcher: CoroutineDispatcher = coroutinesExt.dispatcher,
        response: (RecordedRequest) -> MockResponse? = { null },
    ): RequestExecutor {
        server.start(response)
        val url = server.url("/v4/").toString()
        return createRequestExecutor(
            baseUrl = url,
            authToken = token,
            userAgent = userAgent,
            headers = headers,
            backgroundDispatcher = backgroundDispatcher,
        )
    }
}
