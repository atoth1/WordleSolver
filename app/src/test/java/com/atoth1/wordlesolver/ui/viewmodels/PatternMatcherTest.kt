package com.atoth1.wordlesolver.ui.viewmodels

import com.atoth1.wordlesolver.db.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatternMatcherTest {

    private val testList = listOf(
        Word(0,"aaa",1f),
        Word(1,"bbb",1f),
        Word(2,"ccc",1f),
        Word(3,"ddd",1f),
        Word(4,"eee",1f),
        Word(5,"abc",1f),
        Word(6,"abd",1f),
        Word(7,"abe",1f)
    )

    @Test
    fun patternMatcherTest_correctGuessMatchesOnlyItself() = runTest {
        val guess = "aaa"
        val pattern = Array(3) { BoardViewModel.BoardEntryStatus.HIT }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates.size, 1)
        assertEquals(candidates[0].word, guess)
    }

    @Test
    fun patternMatcherTest_invalidGuessNoMatches() = runTest {
        val guess = "zzz"
        val pattern = Array(3) { BoardViewModel.BoardEntryStatus.HIT }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates.size, 0)
    }

    @Test
    fun patternMatcherTest_allMissesNoChange() = runTest {
        val guess = "zzz"
        val pattern = Array(3) { BoardViewModel.BoardEntryStatus.MISS }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates, testList)
    }

    @Test
    fun patternMatcherTest_wrongLocationCorrectCandidate() = runTest {
        val guess = "bag"
        val pattern = Array(3) {
            if (it == 2) BoardViewModel.BoardEntryStatus.MISS else BoardViewModel.BoardEntryStatus.WRONG_SPOT
        }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates.map { it.word }, listOf("abc", "abd", "abe"))
    }

    @Test
    fun patternMatcherTest_hitCorrectCandidates() = runTest {
        val guess = "azz"
        val pattern = Array(3) {
            if (it == 0) BoardViewModel.BoardEntryStatus.HIT else BoardViewModel.BoardEntryStatus.MISS
        }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates.map { it.word }, listOf("aaa", "abc", "abd", "abe"))
    }

    @Test
    fun patternMatcherTest_hitAndWrongLocationCorrectCandidates() = runTest {
        val guess = "azb"
        val pattern = Array(3) {
            if (it == 0) BoardViewModel.BoardEntryStatus.HIT
            else if (it == 1) BoardViewModel.BoardEntryStatus.MISS
            else BoardViewModel.BoardEntryStatus.WRONG_SPOT
        }
        val candidates = PatternMatcher(guess, pattern).getValidCandidates(testList)
        assertEquals(candidates.map { it.word }, listOf("abc", "abd", "abe"))
    }
}