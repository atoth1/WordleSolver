package com.example.wordlesolver.repository

import com.example.wordlesolver.db.Word
import com.example.wordlesolver.db.WordsDaoInterface
import com.example.wordlesolver.db.WordsDatabaseInterface
import com.example.wordlesolver.network.WordsApiInterface
import kotlinx.coroutines.coroutineScope

interface WordsRepository {

    suspend fun refreshWordList()

    suspend fun getWordList(): List<Word>
}

class WordsRepositoryImpl<DaoType: WordsDaoInterface>(
    private val database: WordsDatabaseInterface<DaoType>,
    private val networkApi: WordsApiInterface
): WordsRepository {

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