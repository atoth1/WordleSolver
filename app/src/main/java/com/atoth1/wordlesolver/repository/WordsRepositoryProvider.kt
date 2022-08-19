package com.atoth1.wordlesolver.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.atoth1.wordlesolver.db.WordsDatabase
import com.atoth1.wordlesolver.network.WordsApi

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