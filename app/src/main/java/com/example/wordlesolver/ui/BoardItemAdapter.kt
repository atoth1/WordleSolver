package com.example.wordlesolver.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.wordlesolver.R

class BoardItemAdapter(
    val registerObservers: (AppCompatButton, Int, Int) -> Unit,
    private val rows: Int,
    private val cols: Int
    ): RecyclerView.Adapter<BoardItemAdapter.LetterViewHolder>() {

    class LetterViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val button: AppCompatButton = view.findViewById(R.id.board_item)
    }

    override fun getItemCount(): Int {
        return rows * cols
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.board_item_view, parent, false)
        return LetterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val row = position/cols
        val col = position - row*cols
        registerObservers(holder.button, row, col)
        holder.button.setOnClickListener {
            val action = BoardFragmentDirections.actionBoardFragmentToLetterFragment(
                position = col
            )
            holder.view.findNavController().navigate(action)
        }
    }
}