package com.atoth1.wordlesolver.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersInterface {

    val mainDispatcher: CoroutineDispatcher

    val ioDispatcher: CoroutineDispatcher

    val defaultDispatcher: CoroutineDispatcher
}

class DefaultDispatchers: DispatchersInterface {

    override val mainDispatcher
        get() = Dispatchers.Main

    override val ioDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    override val defaultDispatcher: CoroutineDispatcher
        get() = Dispatchers.Default
}

object DispatchersProvider {

    var dispatchers: DispatchersInterface = DefaultDispatchers()

    fun resetToDefault() {
        dispatchers = DefaultDispatchers()
    }
}