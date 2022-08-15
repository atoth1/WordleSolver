package com.example.wordlesolver.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordlesolver.repository.FakeApi
import com.example.wordlesolver.repository.FakeDao
import com.example.wordlesolver.repository.FakeDatabase
import com.example.wordlesolver.repository.WordsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    private lateinit var repository: WordsRepositoryImpl<FakeDao>
    private lateinit var viewModel: BoardViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val responseString = "aaaaa\nbbbbb\nccccc\nddddd\neeeee\nfffff\n"
        val fakeApi = FakeApi(responseString)
        val fakeDao = FakeDao(mutableListOf())
        val fakeDatabase = FakeDatabase(fakeDao)
        repository = WordsRepositoryImpl(fakeDatabase, fakeApi)
    }

    private suspend fun checkInitialState() {
        assertEquals(repository.getWordList().size, 6)
        assertEquals(viewModel.activeRow.value, 0)
        val expectedEntry = BoardViewModel.BoardEntry(' ', BoardViewModel.BoardEntryStatus.UNSET)
        for (row in 0 until NUM_ROWS) {
            for (col in 0 until NUM_COLS) {
                assertEquals(viewModel.boardData(row, col).value, expectedEntry)
            }
        }
        assertEquals(viewModel.suggestedWord.value, "raise")
        assertEquals(viewModel.remainingCandidates.value, true)
        assertFalse(viewModel.gameCompleted())
    }

    @Test
    fun boardViewModelTest_initialState() = runTest {
        // Would like to do this in the Before block, but testScheduler
        // and advanceUntilIdle aren't available until here
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        checkInitialState()
    }

    @Test
    fun boardViewModelTest_submitIncorrectValidWord() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        val entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.MISS)
        for (col in 0 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.VALID_WORD)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(viewModel.activeRow.getOrAwaitValue(), 1)
        assertEquals(viewModel.suggestedWord.getOrAwaitValue(), "bbbbb")
    }

    @Test
    fun boardViewModelTest_submitCorrectWord() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        val entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.HIT)
        for (col in 0 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.VALID_WORD)
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.gameCompleted())
        assertEquals(viewModel.activeRow.getOrAwaitValue(), NUM_ROWS)
        assertEquals(viewModel.suggestedWord.getOrAwaitValue(), "")
    }

    @Test
    fun boardViewModelTest_attemptSubmitInvalidWord() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        val entry = BoardViewModel.BoardEntry('g', BoardViewModel.BoardEntryStatus.MISS)
        for (col in 0 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.INVALID_WORD)
    }

    @Test
    fun boardViewModelTest_attemptSubmitIncompleteWord() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        val entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.MISS)
        for (col in 0 until NUM_COLS - 1) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.INCOMPLETE_WORD)
    }

    @Test
    fun boardViewModelTest_submitConflictingInformation() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        var entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.MISS)
        for (col in 0 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.VALID_WORD)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(viewModel.activeRow.getOrAwaitValue(), 1)

        for (col in 1 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(1, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.HIT)
        viewModel.setBoardEntry(0, entry)
        val newEntry = viewModel.boardData(1, 0).getOrAwaitValue()
        assertEquals(newEntry, entry)
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.VALID_WORD)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(viewModel.activeRow.getOrAwaitValue(), NUM_ROWS)
        assertEquals(viewModel.remainingCandidates.getOrAwaitValue(), false)
    }

    @Test
    fun boardViewModelTest_reset() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        viewModel = BoardViewModel(repository, dispatcher, dispatcher)
        advanceUntilIdle()

        val entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.HIT)
        for (col in 0 until NUM_COLS) {
            viewModel.setBoardEntry(col, entry)
            val newEntry = viewModel.boardData(0, col).getOrAwaitValue()
            assertEquals(newEntry, entry)
        }
        assertEquals(viewModel.canSubmit(), BoardViewModel.WordStatus.VALID_WORD)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(viewModel.activeRow.getOrAwaitValue(), NUM_ROWS)
        assertTrue(viewModel.gameCompleted())

        viewModel.reset()
        checkInitialState()
    }
}