package com.example.wordlesolver.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull @ColumnInfo(name = "word") val word: String,
    @NonNull @ColumnInfo(name = "frequency_score") val frequencyScore: Float
)
