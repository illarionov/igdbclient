/*
 * Copyright (c) 2023, the Igdbclient project authors and contributors. Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.igdbclient.dsl

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@IgdbClientDsl
@Suppress("FLOAT_IN_ACCURATE_CALCULATIONS")
public class RetryPolicy {
    /**
     * Enabled automatic retry on HTTP 429 "Too Many Requests" error.
     *
     * Set to `false` to handle this error manually.
     */
    public var enabled: Boolean = true

    /**
     * Specifies the maximum number of attempts to retry a request.
     *
     * If set to `null` the request will be retried indefinitely.
     */
    public var maxRequestRetries: Int? = 5
        set(value) {
            check(value == null || value > 0) { "maxRequestRetries should be > 0" }
            field = value
        }

    /**
     * Initial retry interval. Applied before the first retry.
     * Also used to calculate intervals for next repetitions.
     *
     * When set to 0, all requests will be retried without delay.
     */
    public var initialDelay: Duration = 25.milliseconds
        set(value) {
            check(value >= Duration.ZERO) { "initialDelay should be >= 0" }
            field = value
        }

    /**
     * Exponential delay scaling factor.
     *
     * If set to 1, the interval between retries will be [initialDelay].
     */
    public var factor: Float = 2f
        set(value) {
            check(value >= 1f) { "factor should be >= 1" }
            field = value
        }

    /**
     * Minimum and maximum value of the delay between retries.
     */
    public var delayRange: ClosedRange<Duration> = Duration.ZERO..2.minutes

    /**
     * The relative jitter factor applied to the interval.
     *
     * * 0.0f: without a jitter.
     * * 1.0f: the final delay will be a random value from 0 to 2 * the current interval without jitter.
     */
    public var jitterFactor: Float = 0.1f
}
