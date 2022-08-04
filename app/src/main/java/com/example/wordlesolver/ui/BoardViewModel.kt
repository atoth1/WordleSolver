package com.example.wordlesolver.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordlesolver.network.WordsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val NUM_ROWS = 6
const val NUM_COLS = 5

// Basing this off this blog post:
// https://medium.com/@tglaiel/the-mathematically-optimal-first-guess-in-wordle-cbcb03c19b0a
private const val SUGGESTED_START_WORD = "RAISE"

class BoardViewModel: ViewModel() {

    private val _activeRow = MutableLiveData<Int>(0)
    val activeRow: LiveData<Int> = _activeRow

    enum class BoardEntryStatus {
        UNSET,
        MISS,
        WRONG_SPOT,
        HIT
    }

    data class BoardEntry(val character: Char, val status: BoardEntryStatus)

    private val _boardData = Array(NUM_ROWS) { Array(NUM_COLS) {
        MutableLiveData(BoardEntry(' ', BoardEntryStatus.UNSET)) }
    }
    fun boardData(row: Int, col: Int): LiveData<BoardEntry> {
        return if (row < NUM_ROWS && col < NUM_COLS) {
            _boardData[row][col]
        } else {
            MutableLiveData()
        }
    }

    private val _suggestedWord = MutableLiveData(SUGGESTED_START_WORD)
    val suggestedWord: LiveData<String> = _suggestedWord

    private lateinit var allWords: MutableList<String>
    private lateinit var remainingWords: MutableList<String>

    init {
        viewModelScope.launch {
            try {
                val response = WordsApi.retrofitService.getWords()
                allWords = response.lines().toMutableList()
                remainingWords = response.lines().toMutableList()
                // Either a trailing new line character or null terminator gives extra empty string
                allWords.removeLast()
                remainingWords.removeLast()
            } catch (e: Exception) {
                // Ignoring this for now
            }
        }
    }

    private var lastSubmissionMatched = false

    fun setBoardEntry(col: Int, entry: BoardEntry) {
        if (col < NUM_COLS) {
            _activeRow.value?.let { row ->
                if (row < NUM_ROWS) _boardData[row][col].value = entry
            }
        }
    }

    fun canSubmit(): Boolean {
        var canAdvance = true
        var matches = 0
        for (col in 0 until NUM_COLS) {
            _activeRow.value?.let { row ->
                _boardData[row][col].value?.status.let {
                    if (it == BoardEntryStatus.UNSET) canAdvance = false
                    else if (it == BoardEntryStatus.HIT) ++matches
                }
            }
            if (!canAdvance) break
        }
        if (matches == NUM_COLS) lastSubmissionMatched = true
        return canAdvance
    }

    fun submit()  {
        if (lastSubmissionMatched) _activeRow.value = NUM_ROWS
        else _activeRow.value?.let { _activeRow.value = it + 1 }
        updateSuggestedWord()
    }

    fun gameCompleted(): Boolean {
        return activeRow.value == NUM_ROWS
    }

    private fun updateSuggestedWord() {
        _suggestedWord.value = if (gameCompleted())  ""
        else {
            // Work to figure out remaining possibilities here and get new suggestion
            SUGGESTED_START_WORD
        }
    }

    fun reset() {
        _activeRow.value = 0
        for (row in 0 until NUM_ROWS) {
            for (col in 0 until NUM_COLS) {
                _boardData[row][col].value = BoardEntry(' ', BoardEntryStatus.UNSET)
            }
        }
        _suggestedWord.value = SUGGESTED_START_WORD
        remainingWords = allWords.toMutableList()
        lastSubmissionMatched = false
    }
}