/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

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
