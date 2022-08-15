package com.example.wordlesolver

import android.app.Application
import com.example.wordlesolver.repository.WordsRepositoryInterface
import com.example.wordlesolver.repository.WordsRepositoryProvider

class WordsApplication : Application() {
    val repository: WordsRepositoryInterface
        get() = WordsRepositoryProvider.provideWordsRepository(this)
}
