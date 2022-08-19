package com.atoth1.wordlesolver.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.atoth1.wordlesolver.WordsApplication
import com.atoth1.wordlesolver.dispatchers.DispatchersProvider
import com.atoth1.wordlesolver.ui.viewmodels.BoardViewModel
import com.atoth1.wordlesolver.ui.viewmodels.BoardViewModelFactory
import com.atoth1.wordlesolver.ui.viewmodels.NUM_COLS
import com.atoth1.wordlesolver.ui.viewmodels.NUM_ROWS
import com.atoth1.wordlesolver.R
import com.atoth1.wordlesolver.databinding.FragmentBoardBinding


class BoardFragment: Fragment() {

    @VisibleForTesting
    val viewModel: BoardViewModel by activityViewModels {
        val activity = requireNotNull(this.activity) { R.string.null_activity_error }
        BoardViewModelFactory(
            (activity.application as WordsApplication).repository,
            DispatchersProvider.dispatchers
        )
    }

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BoardItemAdapter(::registerButtonObservers, NUM_ROWS, NUM_COLS)
        binding.boardGrid.adapter = adapter
        binding.boardGrid.layoutManager = GridLayoutManager(requireContext(), NUM_COLS)

        viewModel.suggestedWord.observe(viewLifecycleOwner) {
            binding.suggestionText.text = if (viewModel.gameCompleted()) {
                getString(R.string.game_completed)
            } else {
                getString(R.string.suggestion, it)
            }
        }

        viewModel.remainingCandidates.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), R.string.no_candidates, Toast.LENGTH_SHORT).show()
            }
        }

        binding.submitButton.setOnClickListener {
            when (viewModel.canSubmit()) {
                BoardViewModel.WordStatus.VALID_WORD -> {
                    viewModel.submit()
                    Toast.makeText(requireContext(), R.string.word_submitted, Toast.LENGTH_SHORT).show()
                }
                BoardViewModel.WordStatus.INVALID_WORD -> {
                    Toast.makeText(requireContext(), R.string.invalid_word, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), R.string.incomplete_word, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
        }

        viewModel.activeRow.observe(viewLifecycleOwner) {
            binding.submitButton.isEnabled = it < NUM_ROWS
        }

        viewModel.loadStatus.observe(viewLifecycleOwner) { status ->
            binding.apply {
                when (status) {
                    BoardViewModel.LoadStatus.DONE -> {
                        statusIcon.visibility = View.GONE
                    }
                    BoardViewModel.LoadStatus.LOADING -> {
                        statusIcon.visibility = View.VISIBLE
                        statusIcon.setImageResource(R.drawable.ic_loading_animation)
                    }
                    BoardViewModel.LoadStatus.ERROR -> {
                        statusIcon.visibility = View.VISIBLE
                        statusIcon.setImageResource(R.drawable.ic_connection_error)
                        submitButton.isEnabled = false
                        resetButton.isEnabled = false
                        suggestionText.text = null
                        connectionErrorText.text = getString(R.string.no_connection)
                        Toast.makeText(requireContext(), R.string.no_connection_toast, Toast.LENGTH_LONG)
                            .show()
                    }
                    else -> {
                        // Nothing to do, just supressing possibly null warning
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerButtonObservers(
        button: AppCompatButton, row: Int, col: Int
    ) {
        viewModel.activeRow.observe(viewLifecycleOwner) {
            button.isEnabled = it == row
        }

        val gridData = viewModel.boardData(row, col)
        gridData.observe(viewLifecycleOwner) {
            button.text = it.character.toString()

            val flag = context?.resources?.configuration?.uiMode?.and(
                Configuration.UI_MODE_NIGHT_MASK)
            val darkTheme = flag == Configuration.UI_MODE_NIGHT_YES
            button.text = it.character.toString()
            val color = when (it.status) {
                BoardViewModel.BoardEntryStatus.UNSET -> {
                    if (darkTheme) R.color.black else R.color.white
                } BoardViewModel.BoardEntryStatus.MISS -> {
                    R.color.dark_gray
                } BoardViewModel.BoardEntryStatus.WRONG_SPOT -> {
                    R.color.yellow
                } BoardViewModel.BoardEntryStatus.HIT -> {
                    R.color.green
                }
            }
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), color))
        }
    }
}