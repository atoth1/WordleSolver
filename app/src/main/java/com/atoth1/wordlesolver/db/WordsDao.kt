package com.atoth1.wordlesolver.db

import androidx.room.*

interface  WordsDaoInterface {
    suspend fun getWords(): List<Word>
    suspend fun insertWord(word: Word)
    suspend fun  clearWords()
}

@Dao
interface WordsDao: WordsDaoInterface {
    @Query("SELECT * FROM words ORDER BY frequency_score DESC")
    override suspend fun getWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertWord(word: Word)

    @Query("DELETE FROM words")
    override suspend fun clearWords()
}