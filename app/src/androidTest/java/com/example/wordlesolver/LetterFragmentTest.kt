package com.example.wordlesolver

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.wordlesolver.fakerepository.FakeApi
import com.example.wordlesolver.fakerepository.FakeDao
import com.example.wordlesolver.fakerepository.FakeDatabase
import com.example.wordlesolver.repository.*
import com.example.wordlesolver.ui.LetterFragment
import com.example.wordlesolver.ui.POSITION
import com.example.wordlesolver.ui.viewmodels.BoardViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.random.Random

@LargeTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LetterFragmentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        val dao = FakeDao(mutableListOf())
        val database = FakeDatabase(dao)
        val apiString = "abcde\n"
        val api = FakeApi(apiString)
        val repositoryInterface = WordsRepository(database, api)
        WordsRepositoryProvider.wordsRepository = repositoryInterface
    }

    @After
    fun teardown() {
        WordsRepositoryProvider.wordsRepository = null
    }

    @Test
    fun letterFragmentTest_radioGroup() = runTest {
        val scenario = launchFragmentInContainer<LetterFragment>(Bundle(), R.style.Theme_WordleSolver)
        onView(withId(R.id.miss)).check(matches(not(isChecked())))
        onView(withId(R.id.wrong_spot)).check(matches(not(isChecked())))
        onView(withId(R.id.hit)).check(matches(not(isChecked())))
        scenario.onFragment { fragment ->
            assertEquals(fragment.status, BoardViewModel.BoardEntryStatus.UNSET)
        }
T
        onView(withId(R.id.miss)).perform(click())
        advanceUntilIdle()
        scenario.onFragment { fragment ->
            assertEquals(fragment.status, BoardViewModel.BoardEntryStatus.MISS)
        }
        onView(withId(R.id.miss)).check(matches(isChecked()))
        onView(withId(R.id.wrong_spot)).check(matches(not(isChecked())))
        onView(withId(R.id.hit)).check(matches(not(isChecked())))

        onView(withId(R.id.wrong_spot)).perform(click())
        advanceUntilIdle()
        scenario.onFragment { fragment ->
            assertEquals(fragment.status, BoardViewModel.BoardEntryStatus.WRONG_SPOT)
        }
        onView(withId(R.id.miss)).check(matches(not(isChecked())))
        onView(withId(R.id.wrong_spot)).check(matches(isChecked()))
        onView(withId(R.id.hit)).check(matches(not(isChecked())))

        onView(withId(R.id.hit)).perform(click())
        advanceUntilIdle()
        scenario.onFragment { fragment ->
            assertEquals(fragment.status, BoardViewModel.BoardEntryStatus.HIT)
        }
        onView(withId(R.id.miss)).check(matches(not(isChecked())))
        onView(withId(R.id.wrong_spot)).check(matches(not(isChecked())))
        onView(withId(R.id.hit)).check(matches(isChecked()))
    }

    @Test
    fun letterFragmentTest_lettersNavigate() = runTest {
        val scenario = launchFragmentInContainer<LetterFragment>(
            bundleOf(POSITION to 1),
            R.style.Theme_WordleSolver
        )
        val navController = Mockito.mock(NavController::class.java)
        // Toast if status isn't set
        onView(withId(R.id.miss)).perform(click())
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.view!!, navController)
        }
        for (id in 0 until 26) {
            onView(withId(R.id.letter_grid)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(id, click()))
            val expectedEntry = BoardViewModel.BoardEntry(
                'A'.plus(id),
                BoardViewModel.BoardEntryStatus.MISS
            )
            scenario.onFragment { fragment ->
                // viewModel.activeRow == 0 and POSITION argument was set to 1
                assertEquals(
                    fragment.viewModel.boardData(0, 1).value,
                    expectedEntry
                )
            }
        }
        verify(navController, times(26)).navigateUp()
    }
}