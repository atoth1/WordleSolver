package com.atoth1.wordlesolver.repository

import com.atoth1.wordlesolver.db.Word
import com.atoth1.wordlesolver.db.WordsDatabaseInterface
import com.atoth1.wordlesolver.network.WordsApiInterface
import kotlinx.coroutines.coroutineScope

interface WordsRepositoryInterface {

    suspend fun refreshWordList()

    suspend fun getWordList(): List<Word>
}

class WordsRepository(
    private val database: WordsDatabaseInterface,
    private val networkApi: WordsApiInterface
): WordsRepositoryInterface {

    override suspend fun refreshWordList() {
        val response = networkApi.getWords()
        val allWords = response.lines().toMutableList()
        // Seems like a trailing newline character is giving extra empty string
        allWords.removeLast()

        val previous = getWordList()
        // We'll assume no change if size didn't change
        if (previous.size != allWords.size) {
            database.getDao().clearWords()
            for (str in allWords) {
                coroutineScope {
                    database.getDao()
                        .insertWord(Word(word = str, frequencyScore = str.frequencyScore()))
                }
            }
        }
    }

    override suspend fun getWordList(): List<Word> = database.getDao().getWords()
}