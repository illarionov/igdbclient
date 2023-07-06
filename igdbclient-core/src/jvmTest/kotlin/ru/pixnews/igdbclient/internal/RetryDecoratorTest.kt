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
package ru.pixnews.igdbclient.internal

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.IgdbResult.Success
import ru.pixnews.igdbclient.apicalypse.ApicalypseQuery.Companion.apicalypseQuery
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.internal.IgdbRequest.ApicalypsePostRequest
import ru.pixnews.igdbclient.library.test.MainCoroutineExtension
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RetryDecoratorTest {
    @JvmField
    @RegisterExtension
    var coroutinesExt: MainCoroutineExtension = MainCoroutineExtension()

    @Test
    fun `retryDecorator should skip success and non-429 responses`() = coroutinesExt.runTest {
        val igdbExecutor = TracingRequestExecutor { _, _ -> Success("Test Response") }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _, _ -> "" }),
        )

        (result as? Success<String>)?.value shouldBe "Test Response"
        igdbExecutor.invokeCount shouldBe 1
    }

    @Test
    fun `retryDecorator should retry on 429 responses`() = coroutinesExt.runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L, 2L -> createHttpFailure429TooManyRequests()
                else -> Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _, _ -> "" }),
        )

        (result as? Success<String>)?.value shouldBe "Test Response"
        igdbExecutor.invokeCount shouldBe 3
    }

    @Test
    fun `retryDecorator should sleep between retries`() = coroutinesExt.runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                in 1L..4L -> createHttpFailure429TooManyRequests()
                else -> Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence { 1.minutes } },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _, _ -> "" }),
        )

        (result as? Success<String>)?.value shouldBe "Test Response"
        igdbExecutor.invokeCount shouldBe 5
        testScheduler.currentTime shouldBe 4.minutes.inWholeMilliseconds
    }

    @Test
    fun `retryDecorator should retry maximum maxRequests times`() = coroutinesExt.runTest {
        val igdbExecutor = TracingRequestExecutor { _, _ -> createHttpFailure429TooManyRequests() }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
            maxRequests = 10,
        )

        val result: IgdbResult<Any, *> = decorator(
            ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _, _ -> "" }),
        )

        result.shouldBeInstanceOf<HttpFailure<*>>()
        igdbExecutor.invokeCount shouldBe 10
    }

    @Test
    fun `retryDecorator should use the value of the Retry-After header`() = coroutinesExt.runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L -> createHttpFailure429TooManyRequests(
                    retryAfterHeaderValue = "123",
                )

                else -> Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            request = ApicalypsePostRequest(
                path = "endpoint",
                query = apicalypseQuery { },
                successResponseParser = { _, _ -> "" },
            ),
        )

        (result as? Success<String>)?.value shouldBe "Test Response"
        testScheduler.currentTime shouldBe 123.seconds.inWholeMilliseconds
    }

    companion object {
        internal fun createHttpFailure429TooManyRequests(
            retryAfterHeaderValue: String? = null,
        ): HttpFailure<IgdbHttpErrorResponse> = HttpFailure(
            httpCode = 429,
            httpMessage = "Too Many Requests",
            response = null,
            rawResponseHeaders = retryAfterHeaderValue?.let { listOf("Retry-After" to it) },
            rawResponseBody = """{"message":"Too Many Requests"}""".toByteArray(),
        )
    }
}
