package com.atoth1.wordlesolver.repository

import com.atoth1.wordlesolver.db.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

private const val testApiString = "aaa\nbbb\nccc\n"
private val aaa = Word(0, "aaa", frequencyOf['a']!!)
private val bbb = Word(0, "bbb", frequencyOf['b']!!)
private val ccc = Word(0, "ccc", frequencyOf['c']!!)

@OptIn(ExperimentalCoroutinesApi::class)
class WordsRepositoryTest {

    @Test
    fun wordRepositoryTest_refreshWithEmptyDatabase() = runTest {
        val dao = FakeDao(mutableListOf())
        val database = FakeDatabase(dao)
        val api = FakeApi(testApiString)
        val repo = WordsRepository(database, api)
        assertTrue(repo.getWordList().isEmpty())
        repo.refreshWordList()
        assertEquals(repo.getWordList(), listOf(aaa, bbb, ccc))
    }

    @Test
    fun wordRepositoryTest_refreshWithChangedResponse() = runTest {
        val dao = FakeDao(mutableListOf(aaa, bbb))
        val database = FakeDatabase(dao)
        val api = FakeApi(testApiString)
        val repo = WordsRepository(database, api)
        assertEquals(repo.getWordList(), listOf(aaa, bbb))
        repo.refreshWordList()
        assertEquals(repo.getWordList(), listOf(aaa, bbb, ccc))
    }

    @Test
    fun wordRepositoryTest_refreshWithUnchangeResponse() = runTest {
        val dao = FakeDao(mutableListOf(aaa, bbb, ccc))
        val database = FakeDatabase(dao)
        val api = FakeApi(testApiString)
        val repo = WordsRepository(database, api)
        assertEquals(repo.getWordList(), listOf(aaa, bbb, ccc))
        repo.refreshWordList()
        assertEquals(repo.getWordList(), listOf(aaa, bbb, ccc))
    }
}