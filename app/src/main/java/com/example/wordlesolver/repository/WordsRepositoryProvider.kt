package com.example.wordlesolver.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.wordlesolver.db.WordsDatabase
import com.example.wordlesolver.network.WordsApi

object WordsRepositoryProvider {

    @Volatile
    var wordsRepository: WordsRepositoryInterface? = null
        @VisibleForTesting set

    private val lock = Any()

    fun provideWordsRepository(context: Context): WordsRepositoryInterface {
        synchronized(lock) {
            return wordsRepository ?: WordsRepository(
                WordsDatabase.getInstance(context), WordsApi
            ).also { wordsRepository = it }
        }
    }
}