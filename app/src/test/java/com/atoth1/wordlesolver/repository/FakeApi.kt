package com.atoth1.wordlesolver.repository

import com.atoth1.wordlesolver.network.WordsApiInterface

class FakeApi(private val wordsString: String): WordsApiInterface {

    override suspend fun getWords(): String {
        return wordsString
    }
}