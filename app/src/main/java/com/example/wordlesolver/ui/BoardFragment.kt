package com.example.wordlesolver.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wordlesolver.R
import com.example.wordlesolver.databinding.FragmentBoardBinding

class BoardFragment: Fragment() {

    private val viewModel: BoardViewModel by activityViewModels()
    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BoardItemAdapter(::registerButtonObservers, NUM_ROWS, NUM_COLS)
        binding.boardGrid.adapter = adapter
        binding.boardGrid.layoutManager = GridLayoutManager(requireContext(), NUM_COLS)

        viewModel.suggestedWord.observe(viewLifecycleOwner) {
            binding.suggestionText.text = if (viewModel.gameCompleted()) {
                "Game completed"
            } else {
                "Suggestted word is: $it"
            }
        }

        binding.submitButton.setOnClickListener {
            if (viewModel.canSubmit()) {
                Toast.makeText(requireContext(), "Word submitted", Toast.LENGTH_SHORT)
                    .show()
                viewModel.submit()
                if (viewModel.activeRow.value == NUM_ROWS) {
                    binding.submitButton.isEnabled = false
                    binding.resetButton.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(requireContext(), "Complete word not entered", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
            binding.submitButton.isEnabled = true
            binding.resetButton.visibility = View.GONE
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