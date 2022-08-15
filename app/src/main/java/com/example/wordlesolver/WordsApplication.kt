package com.example.wordlesolver

import android.app.Application
import com.example.wordlesolver.db.WordsDao
import com.example.wordlesolver.db.WordsDatabase

class WordsApplication : Application() {
    val database: WordsDatabase by lazy { WordsDatabase.getInstance(this) }
}
