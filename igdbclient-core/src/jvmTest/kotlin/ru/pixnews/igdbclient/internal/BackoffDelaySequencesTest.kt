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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class BackoffDelaySequencesTest {
    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("sequenceSource")
    fun `exponentialBackoffDelaySequence should return correct result on correct data`(testSpec: SequenceTestData) {
        val sequence = testSpec.getSequence()

        val result = sequence.take(testSpec.expectedResult.size).toList()

        result shouldBe testSpec.expectedResultDurations
    }

    class SequenceTestData(
        private val base: Duration = 25.milliseconds,
        private val factor: Float = 2f,
        private val delayRange: ClosedRange<Duration> = Duration.ZERO..Duration.INFINITE,
        private val jitterFactor: Float = 0f,
        private val jitterRandomSource: () -> Float = { 0f },
        val expectedResult: List<Int>,
    ) {
        val expectedResultDurations
            get() = expectedResult.map { it.milliseconds }

        fun getSequence(): Sequence<Duration> {
            return exponentialBackoffDelaySequence(
                base = base,
                factor = factor,
                delayRange = delayRange,
                jitterFactor = jitterFactor,
                jitterRandomSource = jitterRandomSource,
            )
        }

        override fun toString(): String {
            return "SequenceTestData(base=$base, factor=$factor, " +
                    "delayRange=$delayRange, jitterFactor=$jitterFactor,  expectedResult=$expectedResult)"
        }
    }

    companion object {
        @JvmStatic
        fun sequenceSource(): List<SequenceTestData> = listOf(
            SequenceTestData(
                expectedResult = listOf(25, 50, 100, 200, 400, 800),
            ),
            SequenceTestData(
                base = Duration.ZERO,
                jitterFactor = 1f,
                jitterRandomSource = TestJitterSource(),
                expectedResult = listOf(0, 0, 0, 0, 0, 0),
            ),
            SequenceTestData(
                factor = 1f,
                expectedResult = listOf(25, 25, 25, 25, 25, 25),
            ),
            SequenceTestData(
                delayRange = 50.milliseconds..200.milliseconds,
                // XXX
                expectedResult = listOf(50, 50, 100, 200, 200, 200),
            ),
            SequenceTestData(
                jitterFactor = 1f,
                jitterRandomSource = TestJitterSource(),
                expectedResult = listOf(
                    25,
                    50 - 50,
                    100 + 100,
                    200,
                    400 - 400,
                    800 + 800,
                ),
            ),
        )

        private class TestJitterSource(
            private val values: List<Float> = listOf(0.5f, 0f, 0.9999999f),
        ) : () -> Float {
            private val position: AtomicInteger = AtomicInteger(0)
            override fun invoke(): Float {
                return values[position.getAndIncrement().mod(values.size)]
            }
        }
    }
}
