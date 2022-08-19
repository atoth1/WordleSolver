package com.atoth1.wordlesolver.fakerepository

import com.atoth1.wordlesolver.network.WordsApiInterface

class FakeApi(private val wordsString: String): WordsApiInterface {

    override suspend fun getWords(): String {
        return wordsString
    }
}