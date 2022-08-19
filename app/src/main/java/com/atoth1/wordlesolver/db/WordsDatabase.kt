package com.atoth1.wordlesolver.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

interface WordsDatabaseInterface {
    fun getDao(): WordsDaoInterface
}

@Database(
    entities = [Word::class],
    version = 1,
    exportSchema = false
)
abstract class WordsDatabase : RoomDatabase(), WordsDatabaseInterface {

    abstract fun wordsDao(): WordsDao

    override fun getDao(): WordsDaoInterface = wordsDao()

    companion object {

        @Volatile
        private var INSTANCE: WordsDatabase? = null

        fun getInstance(context: Context): WordsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WordsDatabase::class.java, "word_list.db"
            )
                .build()
    }
}
