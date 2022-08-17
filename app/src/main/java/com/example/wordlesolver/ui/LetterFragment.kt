package com.example.wordlesolver.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wordlesolver.R
import com.example.wordlesolver.WordsApplication
import com.example.wordlesolver.databinding.FragmentLetterBinding
import com.example.wordlesolver.dispatchers.DispatchersProvider
import com.example.wordlesolver.ui.viewmodels.BoardViewModel
import com.example.wordlesolver.ui.viewmodels.BoardViewModelFactory

const val POSITION = "position"

private const val GRID_COLS = 4

class LetterFragment: Fragment() {

    @VisibleForTesting
    val viewModel: BoardViewModel by activityViewModels {
        val activity = requireNotNull(this.activity) { getString(R.string.null_activity_error) }
        BoardViewModelFactory(
            (activity.application as WordsApplication).repository,
            DispatchersProvider.dispatchers
        )
    }

    private var _binding: FragmentLetterBinding? = null
    private val binding get() = _binding!!

    private var _status = BoardViewModel.BoardEntryStatus.UNSET
    val status get() = _status
    private var pos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val tmp = it.getInt(POSITION)
            if (tmp in 0 until GRID_COLS) pos = tmp
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLetterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.miss.setOnClickListener {
            _status = BoardViewModel.BoardEntryStatus.MISS
        }
        binding.wrongSpot.setOnClickListener {
            _status = BoardViewModel.BoardEntryStatus.WRONG_SPOT
        }
        binding.hit.setOnClickListener {
            _status = BoardViewModel.BoardEntryStatus.HIT
        }
        val adapter = LetterItemAdapter {
            if (_status == BoardViewModel.BoardEntryStatus.UNSET) {
                Toast.makeText(requireContext(), R.string.unset_result, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setBoardEntry(pos, BoardViewModel.BoardEntry(it, _status))
                findNavController().navigateUp()
            }
        }
        binding.letterGrid.adapter = adapter
        binding.letterGrid.layoutManager = GridLayoutManager(requireContext(), GRID_COLS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}