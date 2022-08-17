package com.example.wordlesolver.fakerepository

import com.example.wordlesolver.network.WordsApiInterface

class FakeApi(private val wordsString: String): WordsApiInterface {

    override suspend fun getWords(): String {
        return wordsString
    }
}