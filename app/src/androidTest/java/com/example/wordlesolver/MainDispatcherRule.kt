package com.example.wordlesolver

import com.example.wordlesolver.dispatchers.DispatchersProvider
import com.example.wordlesolver.dispatchers.TestDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    private val dispatchers = TestDispatchers(testDispatcher)

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
        DispatchersProvider.dispatchers  = dispatchers
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
        DispatchersProvider.resetToDefault()
    }
}
