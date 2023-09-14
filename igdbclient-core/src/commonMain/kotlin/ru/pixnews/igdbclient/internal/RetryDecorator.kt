/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.internal

import kotlinx.coroutines.delay
import ru.pixnews.igdbclient.IgdbResult
import ru.pixnews.igdbclient.IgdbResult.Failure.HttpFailure
import ru.pixnews.igdbclient.error.IgdbHttpErrorResponse
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class RetryDecorator(
    private val delaySequenceFactory: () -> Sequence<Duration>,
    private val delegate: RequestExecutor,
    private val maxRequests: Int? = 5,
) : RequestExecutor {
    internal constructor(
        initialInterval: Duration,
        factor: Float,
        maxAttempts: Int?,
        delayRange: ClosedRange<Duration>,
        jitterFactor: Float,
        jitterRandomSource: () -> Float = Random.Default::nextFloat,
        delegate: RequestExecutor,
    ) : this(
        {
            exponentialBackoffDelaySequence(
                initialInterval,
                factor,
                delayRange,
                jitterFactor,
                jitterRandomSource,
            )
        },
        delegate,
        maxAttempts,
    )

    override suspend fun <T : Any> invoke(request: IgdbRequest): IgdbResult<T, IgdbHttpErrorResponse> {
        var lastResponse: IgdbResult<T, IgdbHttpErrorResponse>
        val sequenceIterator = delaySequenceFactory().iterator()
        var requestsCount = 0
        val maxRequests = maxRequests
        while (true) {
            lastResponse = delegate.invoke(request)
            if (!lastResponse.is429TooMnyRequests()) {
                return lastResponse
            }
            if (maxRequests != null) {
                requestsCount += 1
                if (requestsCount >= maxRequests) {
                    return lastResponse
                }
            }

            val retryAfter = (lastResponse as? HttpFailure<*>)?.retryAfterHeaderValue()

            val delayDuration = if (retryAfter != null) {
                retryAfter
            } else {
                if (!sequenceIterator.hasNext()) {
                    return lastResponse
                }
                sequenceIterator.next()
            }

            delay(delayDuration)
        }
    }

    internal companion object {
        private val westernDigits = '0'..'9'
        private fun HttpFailure<*>.retryAfterHeaderValue(): Duration? = this.rawResponseHeaders
            ?.firstNotNullOfOrNull {
                if ("Retry-After".equals(it.first, ignoreCase = true)) it.second else null
            }
            ?.parseRetryAfterHeaderOrNull()

        private fun String.parseRetryAfterHeaderOrNull(): Duration? {
            return if (this.all { it in westernDigits }) {
                return toLongOrNull()?.seconds
            } else {
                null
            }
        }

        @Suppress("MagicNumber")
        private fun IgdbResult<*, *>.is429TooMnyRequests() = this is HttpFailure<*> && this.httpCode == 429
    }
}
