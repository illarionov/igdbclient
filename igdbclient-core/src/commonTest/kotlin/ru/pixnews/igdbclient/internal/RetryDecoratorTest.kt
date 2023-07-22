/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package ru.pixnews.igdbclient.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlinx.coroutines.test.runTest
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.apicalypse.apicalypseQuery
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import ru.pixnews.igdbclient.test.TracingRequestExecutor
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RetryDecoratorTest {
    @Test
    fun retryDecorator_should_skip_success_and_non_429_responses() = runTest {
        val igdbExecutor = TracingRequestExecutor { _, _ -> IgdbResult.Success("Test Response") }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            IgdbRequest.ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _ -> "" }),
        )

        assertThat(result)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo("Test Response")

        assertThat(igdbExecutor.invokeCount).isEqualTo(1)
    }

    @Test
    fun retryDecorator_should_retry_on_429_responses() = runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L, 2L -> createHttpFailure429TooManyRequests()
                else -> IgdbResult.Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            IgdbRequest.ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _ -> "" }),
        )

        assertThat(result)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo("Test Response")

        assertThat(igdbExecutor.invokeCount).isEqualTo(3)
    }

    @Test
    fun retryDecorator_should_sleep_between_retries() = runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                in 1L..4L -> createHttpFailure429TooManyRequests()
                else -> IgdbResult.Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence { 1.minutes } },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            IgdbRequest.ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _ -> "" }),
        )

        assertThat(result)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo("Test Response")
        assertThat(igdbExecutor.invokeCount).isEqualTo(5)

        assertThat(testScheduler.currentTime).isEqualTo(4.minutes.inWholeMilliseconds)
    }

    @Test
    fun retryDecorator_should_retry_maximum_maxRequests_times() = runTest {
        val igdbExecutor = TracingRequestExecutor { _, _ -> createHttpFailure429TooManyRequests() }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
            maxRequests = 10,
        )

        val result: IgdbResult<Any, *> = decorator(
            IgdbRequest.ApicalypsePostRequest("endpoint", apicalypseQuery { }, { _ -> "" }),
        )

        assertThat(result).isInstanceOf<IgdbResult.Failure.HttpFailure<*>>()
        assertThat(igdbExecutor.invokeCount).isEqualTo(10)
    }

    @Test
    fun retryDecorator_should_use_the_value_of_the_Retry_After_header() = runTest {
        val igdbExecutor = TracingRequestExecutor { _, requestNo ->
            when (requestNo) {
                1L -> createHttpFailure429TooManyRequests(
                    retryAfterHeaderValue = "123",
                )

                else -> IgdbResult.Success("Test Response")
            }
        }
        val decorator = RetryDecorator(
            delaySequenceFactory = { generateSequence(Duration::ZERO) },
            delegate = igdbExecutor,
        )

        val result: IgdbResult<String, *> = decorator.invoke(
            request = IgdbRequest.ApicalypsePostRequest(
                path = "endpoint",
                query = apicalypseQuery { },
                successResponseParser = { _ -> "" },
            ),
        )

        assertThat(result)
            .isInstanceOf<IgdbResult.Success<String>>()
            .prop(IgdbResult.Success<String>::value).isEqualTo("Test Response")

        assertThat(testScheduler.currentTime).isEqualTo(123.seconds.inWholeMilliseconds)
    }

    companion object {
        internal fun createHttpFailure429TooManyRequests(
            retryAfterHeaderValue: String? = null,
        ): IgdbResult.Failure.HttpFailure<IgdbHttpErrorResponse> = IgdbResult.Failure.HttpFailure(
            httpCode = 429,
            httpMessage = "Too Many Requests",
            response = null,
            rawResponseHeaders = retryAfterHeaderValue?.let { listOf("Retry-After" to it) },
            rawResponseBody = """{"message":"Too Many Requests"}""".encodeToByteArray(),
        )
    }
}
