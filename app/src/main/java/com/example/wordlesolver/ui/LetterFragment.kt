package com.example.wordlesolver.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class LetterFragment: Fragment() {

    private val viewModel: BoardViewModel by activityViewModels {
        val activity = requireNotNull(this.activity) { getString(R.string.null_activity_error) }
        BoardViewModelFactory(
            (activity.application as WordsApplication).repository,
            DispatchersProvider.ioDispatcher,
            DispatchersProvider.defaultDispatcher
        )
    }

    private var _binding: FragmentLetterBinding? = null
    private val binding get() = _binding!!

    private var status = BoardViewModel.BoardEntryStatus.UNSET
    private var pos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            pos = it.getInt(POSITION)
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
            status = BoardViewModel.BoardEntryStatus.MISS
        }
        binding.wrongSpot.setOnClickListener {
            status = BoardViewModel.BoardEntryStatus.WRONG_SPOT
        }
        binding.hit.setOnClickListener {
            status = BoardViewModel.BoardEntryStatus.HIT
        }
        val adapter = LetterItemAdapter {
            if (status == BoardViewModel.BoardEntryStatus.UNSET) {
                Toast.makeText(requireContext(), R.string.unset_result, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setBoardEntry(pos, BoardViewModel.BoardEntry(it, status))
                findNavController().navigateUp()
            }
        }
        binding.letterGrid.adapter = adapter
        binding.letterGrid.layoutManager = GridLayoutManager(requireContext(), 4)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}