/*
 * Copyright (c) 2023-2025, the Igdbclient project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.igdbclient.internal

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class BackoffDelaySequencesTest {
    @Test
    fun exponentialBackoffDelaySequence_should_return_correct_result_on_correct_data() {
        val sequence = exponentialBackoffDelaySequence(
            base = 25.milliseconds,
            factor = 2f,
            delayRange = Duration.ZERO..Duration.INFINITE,
            jitterFactor = 0f,
            jitterRandomSource = { 0f },
        )

        val result = sequence.takeAsListInMilliseconds(6)

        assertThat(result).containsExactly(25, 50, 100, 200, 400, 800)
    }

    @Test
    fun exponentialBackoffDelaySequence_should_return_zero_if_base_is_zero() {
        val jitterSource = TestJitterSource(listOf(0.5f, 0f, 0.9999999f))
        val sequence = exponentialBackoffDelaySequence(
            base = Duration.ZERO,
            factor = 2f,
            delayRange = Duration.ZERO..Duration.INFINITE,
            jitterFactor = 1f,
            jitterRandomSource = jitterSource::invoke,
        )

        val result = sequence.takeAsListInMilliseconds(6)

        assertThat(result).containsExactly(0, 0, 0, 0, 0, 0)
    }

    @Test
    fun exponentialBackoffDelaySequence_should_return_correct_when_factor_is_1() {
        val sequence = exponentialBackoffDelaySequence(
            base = 25.milliseconds,
            factor = 1f,
            delayRange = Duration.ZERO..Duration.INFINITE,
            jitterFactor = 0f,
            jitterRandomSource = { 0f },
        )

        val result = sequence.takeAsListInMilliseconds(6)

        assertThat(result).containsExactly(25, 25, 25, 25, 25, 25)
    }

    @Test
    fun exponentialBackoffDelaySequence_should_return_delay_constrained_by_delayRange() {
        val sequence = exponentialBackoffDelaySequence(
            base = 25.milliseconds,
            factor = 2f,
            delayRange = 50.milliseconds..200.milliseconds,
            jitterFactor = 0f,
            jitterRandomSource = { 0f },
        )

        val result = sequence.takeAsListInMilliseconds(6)

        // XXX: skip initial identical values generated out of range?
        assertThat(result).containsExactly(50, 50, 100, 200, 200, 200)
    }

    @Test
    fun exponentialBackoffDelaySequence_should_return_correct_values_on_test_jitter_boundary_values() {
        val jitterSource = TestJitterSource(listOf(0.5f, 0f, 0.9999999f))
        val sequence = exponentialBackoffDelaySequence(
            base = 25.milliseconds,
            factor = 2f,
            delayRange = Duration.ZERO..Duration.INFINITE,
            jitterFactor = 1f,
            jitterRandomSource = jitterSource::invoke,
        )

        val result = sequence.takeAsListInMilliseconds(6)

        assertThat(result).containsExactly(
            25,
            50 - 50,
            100 + 100,
            200,
            400 - 400,
            800 + 800,
        )
    }

    @Suppress("IDENTIFIER_LENGTH")
    private fun Sequence<Duration>.takeAsListInMilliseconds(n: Int) = take(n)
        .map { it.inWholeMilliseconds.toInt() }
        .toList()

    private class TestJitterSource(
        private val values: List<Float>,
    ) {
        private val position: AtomicInt = atomic(0)
        operator fun invoke(): Float {
            return values[position.getAndIncrement().mod(values.size)]
        }
    }
}
