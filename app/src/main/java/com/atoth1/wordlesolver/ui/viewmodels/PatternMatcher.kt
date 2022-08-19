package com.atoth1.wordlesolver.ui.viewmodels

import com.atoth1.wordlesolver.db.Word
import kotlinx.coroutines.*

const val INVALID_STRING = "NOT A MATCH"

// 'guess' and 'pattern' represent the actual results from guessing a word,
// intention to get words which would give this 'pattern' when given 'guess'
// if they were the solution
class PatternMatcher(
    private val guess: String,
    private val pattern: Array<BoardViewModel.BoardEntryStatus>
) {

    suspend fun getValidCandidates(candidates: List<Word>): MutableList<Word> {
        val remainingCandidates = mutableListOf<Deferred<Word>>()
        coroutineScope {
            candidates.forEach { candidate ->
                val deferred = async {
                    if (matchingPattern(candidate.word, pattern)) {
                        candidate
                    } else {
                        Word(candidate.id, INVALID_STRING, 0f)
                    }
                }
                remainingCandidates.add(deferred)
            }
        }
        val list = remainingCandidates.awaitAll()
        return list.filter { it.word != INVALID_STRING }.toMutableList()
    }

    private fun matchingPattern(
        candidate: String,
        pattern: Array<BoardViewModel.BoardEntryStatus>
    ): Boolean {
        if (candidate.length != guess.length) return false
        // Compute pattern that would occur if candidate were the solution with given guess
        val candidatePattern = Array(pattern.size) { BoardViewModel.BoardEntryStatus.UNSET }
        // Check for exact matches
        for (id in candidate.indices) {
            if (candidate[id] == guess[id]) {
                candidatePattern[id] = BoardViewModel.BoardEntryStatus.HIT
            }
        }

        // Check for correct letters in wrong position
        val residualCounts = mutableMapOf<Char, Int>()
        candidate.forEachIndexed() { id, c ->
            // Skip letters which are exact match
            if (candidatePattern[id] == BoardViewModel.BoardEntryStatus.UNSET) {
                residualCounts[c] = residualCounts[c]?.let { it + 1 } ?: 1
            }
        }
        guess.forEachIndexed { id, c ->
            if (candidatePattern[id] == BoardViewModel.BoardEntryStatus.UNSET) {
                candidatePattern[id] = residualCounts[c]?.let {
                    if (it > 1) {
                        residualCounts[c] = it - 1
                    } else run {
                        residualCounts.remove(c)
                    }
                    BoardViewModel.BoardEntryStatus.WRONG_SPOT
                } ?: BoardViewModel.BoardEntryStatus.MISS
            }
        }

        // Equal to the actual pattern
        return candidatePattern.contentEquals(pattern)
    }
}