package com.atoth1.wordlesolver.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatchers(
    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()
): DispatchersInterface {

    override val mainDispatcher: CoroutineDispatcher
        get() = testDispatcher

    override val ioDispatcher: CoroutineDispatcher
        get() = testDispatcher

    override val defaultDispatcher: CoroutineDispatcher
        get() = testDispatcher
}