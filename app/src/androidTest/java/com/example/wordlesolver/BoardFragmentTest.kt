package com.example.wordlesolver

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wordlesolver.fakerepository.FakeApi
import com.example.wordlesolver.fakerepository.FakeDao
import com.example.wordlesolver.fakerepository.FakeDatabase
import com.example.wordlesolver.repository.*
import com.example.wordlesolver.ui.BoardFragment
import com.example.wordlesolver.ui.BoardFragmentDirections
import com.example.wordlesolver.ui.viewmodels.BoardViewModel
import com.example.wordlesolver.ui.viewmodels.NUM_COLS
import com.example.wordlesolver.ui.viewmodels.NUM_ROWS
import com.example.wordlesolver.util.RecyclerViewMatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito.*

@LargeTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BoardFragmentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        val dao = FakeDao(mutableListOf())
        val database = FakeDatabase(dao)
        val apiString = "abcde\naaaaa\nbbbbb\nccccc\nddddd\neeeee\n"
        val api = FakeApi(apiString)
        val repositoryInterface = WordsRepository(database, api)
        WordsRepositoryProvider.wordsRepository = repositoryInterface
    }

    @After
    fun teardown() {
        WordsRepositoryProvider.wordsRepository = null
    }

    private fun verifyInitialConfig(context: Context) {
        onView(withId(R.id.submit_button)).check(matches(isEnabled()))
        onView(withId(R.id.submit_button)).check(matches(withText(
            containsString(context.getString(R.string.submit_word)))))
        onView(withId(R.id.reset_button)).check(matches(isEnabled()))
        onView(withId(R.id.reset_button)).check(matches(withText(
            containsString(context.getString(R.string.reset)))))
        onView(withId(R.id.suggestion_text)).check(matches(withText(
            containsString(context.getString(R.string.suggestion, "raise")))))
        for (id in 0 until NUM_COLS) {
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(isEnabled()))
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(withText(" ")))
        }
        for (id in NUM_COLS+1 until NUM_ROWS*NUM_COLS) {
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(not(isEnabled())))
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(withText(" ")))
        }
    }

    @Test
    fun boardFragmentTest_initialConfig() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchFragmentInContainer<BoardFragment>(Bundle(), R.style.Theme_WordleSolver)
        verifyInitialConfig(appContext)
    }

    @Test
    fun boardFragmentTest_resetToInitalConfig() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = launchFragmentInContainer<BoardFragment>(Bundle(), R.style.Theme_WordleSolver)
        advanceUntilIdle()

        scenario.onFragment { fragment ->
            val entry = BoardViewModel.BoardEntry('a', BoardViewModel.BoardEntryStatus.MISS)
            for (col in 0 until NUM_COLS) {
                fragment.viewModel.setBoardEntry(col, entry)
            }
        }
        advanceUntilIdle()

        onView(withId(R.id.submit_button)).perform(click())
        advanceUntilIdle()

        scenario.onFragment { fragment ->
            assertEquals(fragment.viewModel.activeRow.value, 1)
        }
        onView(withId(R.id.reset_button)).perform(click())
        advanceUntilIdle()

        verifyInitialConfig(appContext)
    }

    private fun checkRowContainsString(str: String, startId: Int) {
        for (offset in 0 until NUM_COLS) {
            val id = startId + offset
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(not(isEnabled())))
            onView(RecyclerViewMatcher(R.id.board_grid).atPosition(id)).check(matches(withText(str[offset].toString())))
        }
    }

    private fun verifyFinalConfig(context: Context) {
        onView(withId(R.id.submit_button)).check(matches(not(isEnabled())))
        onView(withId(R.id.submit_button)).check(matches(withText(
            containsString(context.getString(R.string.submit_word)))))
        onView(withId(R.id.reset_button)).check(matches(isEnabled()))
        onView(withId(R.id.reset_button)).check(matches(withText(
            containsString(context.getString(R.string.reset)))))
        onView(withId(R.id.suggestion_text)).check(matches(withText(
            containsString(context.getString(R.string.game_completed)))))
        checkRowContainsString("aaaaa", 0)
        checkRowContainsString("bbbbb", NUM_COLS)
        checkRowContainsString("ccccc", 2 * NUM_COLS)
        checkRowContainsString("ddddd", 3 * NUM_COLS)
        checkRowContainsString("eeeee", 4 * NUM_COLS)
        checkRowContainsString("abcde", 5 * NUM_COLS)
    }

    @Test
    fun boardFragmentTest_submitWords() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = launchFragmentInContainer<BoardFragment>(Bundle(), R.style.Theme_WordleSolver)
        advanceUntilIdle()

        val submissionList = listOf("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee", "abcde")
        submissionList.forEachIndexed { row, word ->
            scenario.onFragment { fragment ->
                for (col in 0 until NUM_COLS) {
                    val status = if (row < NUM_ROWS - 1) {
                        if (col == row) BoardViewModel.BoardEntryStatus.HIT
                        else BoardViewModel.BoardEntryStatus.MISS
                    } else BoardViewModel.BoardEntryStatus.HIT
                    val entry = BoardViewModel.BoardEntry(word[col], status)
                    fragment.viewModel.setBoardEntry(col, entry)
                }
            }
            advanceUntilIdle()

            onView(withId(R.id.submit_button)).perform(click())
            advanceUntilIdle()
        }

        verifyFinalConfig(appContext)
    }

    @Test
    fun boardFragmentTest_lettersNavigate() = runTest {
        val scenario = launchFragmentInContainer<BoardFragment>(Bundle(), R.style.Theme_WordleSolver)
        advanceUntilIdle()

        val navController = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.view!!, navController)
        }

        // Can only click in the active row
        for (col in 0 until NUM_COLS) {
            onView(withId(R.id.board_grid)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(col, click()))
            verify(navController).navigate(
                BoardFragmentDirections.actionBoardFragmentToLetterFragment(position = col)
            )
        }
    }

    // Test entry background color and toasts somehow?
}