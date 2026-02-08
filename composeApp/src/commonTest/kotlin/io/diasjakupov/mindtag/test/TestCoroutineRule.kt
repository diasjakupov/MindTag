package io.diasjakupov.mindtag.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) {
    fun before() {
        Dispatchers.setMain(testDispatcher)
    }

    fun after() {
        Dispatchers.resetMain()
    }
}

/**
 * Convenience for running a test with Main dispatcher overridden.
 * Usage:
 * ```
 * @Test
 * fun myTest() = runTestWithMainDispatcher {
 *     // test body
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runTestWithMainDispatcher(
    testBody: suspend kotlinx.coroutines.test.TestScope.() -> Unit,
) {
    val dispatcher = StandardTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        kotlinx.coroutines.test.runTest(context = dispatcher, testBody = testBody)
    } finally {
        Dispatchers.resetMain()
    }
}
