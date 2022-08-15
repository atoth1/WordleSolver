package com.example.wordlesolver.dispatchers

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatchersProvider {

    var mainDispatcher: CoroutineDispatcher = Dispatchers.Main
        @VisibleForTesting set

    var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        @VisibleForTesting set

    var defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
        @VisibleForTesting set
}