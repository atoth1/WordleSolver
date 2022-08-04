package com.example.wordlesolver.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

// Thanks to this knuth person for the word list.
// No idea if there are any Wordle solution words not actually in this list.
private const val BASE_URL = "https://www-cs-faculty.stanford.edu"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface WordsApiService {
    @GET("~knuth/sgb-words.txt")
    suspend fun getWords(): String
}

object WordsApi {
    val retrofitService : WordsApiService by lazy {
        retrofit.create(WordsApiService::class.java)
    }
}
