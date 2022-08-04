package com.example.wordlesolver.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.wordlesolver.R

class LetterItemAdapter(val onClick: (Char) -> Unit):
    RecyclerView.Adapter<LetterItemAdapter.LetterViewHolder>() {

    // Generates a [CharRange] from 'A' to 'Z' and converts it to a list
    private val list = ('A').rangeTo('Z').toList()

    class LetterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.letter_item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.letter_item_view, parent, false)
        return LetterViewHolder(layout)
    }

    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val item = list[position]
        val text = item.toString()
        holder.button.text = text
        holder.button.setOnClickListener {
            onClick(text[0])
        }
    }
}