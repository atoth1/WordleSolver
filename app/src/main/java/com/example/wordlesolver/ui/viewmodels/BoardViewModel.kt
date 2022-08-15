package com.example.wordlesolver.ui.viewmodels

import androidx.lifecycle.*
import com.example.wordlesolver.db.Word
import com.example.wordlesolver.repository.WordsRepositoryInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val NUM_ROWS = 6
const val NUM_COLS = 5

// Basing this off this blog post:
// https://medium.com/@tglaiel/the-mathematically-optimal-first-guess-in-wordle-cbcb03c19b0a
// Actually comes in 3rd place (tied with arise) by the unique frequency rate metric used here.
private const val SUGGESTED_START_WORD = "raise"

class BoardViewModel(
    private val repository: WordsRepositoryInterface,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher
): ViewModel() {

    private val _activeRow = MutableLiveData(0)
    val activeRow: LiveData<Int> = _activeRow

    enum class BoardEntryStatus {
        UNSET,
        MISS,
        WRONG_SPOT,
        HIT
    }

    data class BoardEntry(val character: Char, val status: BoardEntryStatus)

    private val _boardData = Array(NUM_ROWS) {
        Array(NUM_COLS) {
            MutableLiveData(BoardEntry(' ', BoardEntryStatus.UNSET))
        }
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

    enum class LoadStatus {
        LOADING, ERROR, DONE
    }

    private val _loadStatus = MutableLiveData(LoadStatus.DONE)
    val loadStatus: LiveData<LoadStatus> = _loadStatus

    private var allWords = emptyList<Word>()
    private var remainingWords: MutableList<Word> = mutableListOf()
    private var lookupSet = emptySet<String>()

    init {
        viewModelScope.launch(ioDispatcher) {
            _loadStatus.postValue(LoadStatus.LOADING)
            try {
                repository.refreshWordList()
            } catch (e: Exception) {

            }
            // Might still get cached list if refresh failed
            try {
                allWords = repository.getWordList()
                remainingWords = allWords.toMutableList()
                lookupSet = allWords.map { word -> word.word }.toSet()
            } catch (e: Exception) {

            }
            if (allWords.isEmpty()) {
                _loadStatus.postValue(LoadStatus.ERROR)
            } else {
                _loadStatus.postValue(LoadStatus.DONE)
            }
        }
    }

    fun setBoardEntry(col: Int, entry: BoardEntry) {
        if (col < NUM_COLS) {
            _activeRow.value?.let { row ->
                if (row < NUM_ROWS) _boardData[row][col].value = entry
            }
        }
    }

    private var lastSubmissionMatched = false
    private var acceptedWord = ""
    private val acceptedPattern = Array(NUM_COLS) { BoardEntryStatus.UNSET }

    private fun clearAcceptedPattern() {
        for (id in acceptedPattern.indices) acceptedPattern[id] = BoardEntryStatus.UNSET
    }

    enum class WordStatus {
        INVALID_WORD,
        INCOMPLETE_WORD,
        VALID_WORD
    }

    fun canSubmit(): WordStatus {
        var canSubmit = true
        var hits = 0
        val wordBuilder = StringBuilder()
        for (col in 0 until NUM_COLS) {
            _activeRow.value?.let { row ->
                _boardData[row][col].value?.let {
                    wordBuilder.append(it.character)
                    acceptedPattern[col] = it.status
                    if (it.status == BoardEntryStatus.UNSET) canSubmit = false
                    else if (it.status == BoardEntryStatus.HIT) ++hits
                }
            }
            if (!canSubmit) {
                break
            }
        }

        return if (canSubmit) {
            // All letters were set, make sure it's a valid word
            val candidate = wordBuilder.toString().lowercase()
            if (lookupSet.contains(candidate)) {
                acceptedWord = candidate
                // Pattern already fine
                if (hits == NUM_COLS) lastSubmissionMatched = true
                WordStatus.VALID_WORD
            } else {
                clearAcceptedPattern()
                WordStatus.INVALID_WORD
            }
        } else {
            // StringBuilder is just destroyed
            clearAcceptedPattern()
            WordStatus.INCOMPLETE_WORD
        }
    }

    fun submit()  {
        if (lastSubmissionMatched) _activeRow.value = NUM_ROWS
        else _activeRow.value?.let { _activeRow.value = it + 1 }
        updateSuggestedWord()
    }

    fun gameCompleted(): Boolean {
        return activeRow.value == NUM_ROWS
    }

    private val _remainingCandidates = MutableLiveData(true)
    val remainingCandidates:LiveData<Boolean> = _remainingCandidates

    private fun updateSuggestedWord() {
        if (gameCompleted())  _suggestedWord.value = ""
        else {
            viewModelScope.launch(defaultDispatcher) {
                _loadStatus.postValue(LoadStatus.LOADING)
                remainingWords = PatternMatcher(acceptedWord, acceptedPattern)
                    .getValidCandidates(remainingWords)
                _loadStatus.postValue(LoadStatus.DONE)

                if (remainingWords.isEmpty()) {
                    // Whoops, conflicting info or solution missing from
                    _remainingCandidates.postValue(false)
                    _activeRow.postValue(NUM_ROWS)
                    _suggestedWord.postValue("")
                } else {
                    // Still sorted by decreasing frequency score, so just get the highest
                    _suggestedWord.postValue(remainingWords[0].word)
                }
            }
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
        _remainingCandidates.value = true
        lastSubmissionMatched = false
        acceptedWord = ""
        clearAcceptedPattern()
    }
}

class BoardViewModelFactory(
    private val repository: WordsRepositoryInterface,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoardViewModel(repository, ioDispatcher, defaultDispatcher) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}


