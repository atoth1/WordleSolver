package com.atoth1.wordlesolver

import android.app.Application
import com.atoth1.wordlesolver.repository.WordsRepositoryInterface
import com.atoth1.wordlesolver.repository.WordsRepositoryProvider

class WordsApplication : Application() {
    val repository: WordsRepositoryInterface
        get() = WordsRepositoryProvider.provideWordsRepository(this)
}
