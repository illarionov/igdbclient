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

import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Suppress("FLOAT_IN_ACCURATE_CALCULATIONS", "INVERSE_FUNCTION_PREFERRED")
internal fun exponentialBackoffDelaySequence(
    base: Duration,
    factor: Float = 2f,
    delayRange: ClosedRange<Duration> = Duration.ZERO..Duration.INFINITE,
    jitterFactor: Float = 1f,
    jitterRandomSource: () -> Float,
): Sequence<Duration> {
    check(base >= Duration.ZERO) { "Base duration should be >= 0" }
    check(factor >= 1) { "factor should be >= 1" }
    check(!delayRange.isEmpty()) { "delayRange should not be empty" }
    check(jitterFactor in 0f..1f) { "jitterFactor should not be empty" }

    return sequence {
        var multiplier = 1f
        val initialIntervalMs = base.inWholeMilliseconds
        while (true) {
            val delayMillis = initialIntervalMs * multiplier
            val halfJitterRange = delayMillis * jitterFactor
            val jitter = 2f * halfJitterRange * jitterRandomSource() - halfJitterRange

            val delayWithJitter = (delayMillis + jitter).roundToLong().milliseconds.coerceIn(delayRange)
            yield(delayWithJitter)

            multiplier *= factor
        }
    }
}
