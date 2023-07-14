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
package ru.pixnews.igdbclient.library.test.jupiter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public class MainCoroutineExtension : BeforeEachCallback, AfterEachCallback {
    public lateinit var testScope: TestScope
        private set
    public lateinit var scheduler: TestCoroutineScheduler
        private set
    public lateinit var dispatcher: TestDispatcher
        private set

    override fun beforeEach(context: ExtensionContext?) {
        scheduler = TestCoroutineScheduler()
        dispatcher = StandardTestDispatcher(
            scheduler = scheduler,
            name = "Standard main test dispatcher",
        )
        testScope = TestScope(dispatcher)
        Dispatchers.setMain(dispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }

    public fun runTest(
        timeout: Duration = 60.seconds,
        testBody: suspend TestScope.() -> Unit,
    ): TestResult = testScope.runTest(timeout = timeout, testBody)
}
