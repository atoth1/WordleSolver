package com.example.wordlesolver.fakerepository

import com.example.wordlesolver.db.Word
import com.example.wordlesolver.db.WordsDaoInterface
import com.example.wordlesolver.db.WordsDatabaseInterface

class FakeDao(private val words: MutableList<Word>): WordsDaoInterface {

    override suspend fun getWords(): List<Word> {
        return words
    }

    override suspend fun insertWord(word: Word) {
        words.add(word)
    }

    override suspend fun clearWords() {
        words.clear()
    }
}

class FakeDatabase(private val wordsDao: FakeDao): WordsDatabaseInterface {

    override fun getDao(): WordsDaoInterface {
        return wordsDao
    }
}