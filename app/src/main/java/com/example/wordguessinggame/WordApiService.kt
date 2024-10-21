package com.example.wordguessinggame

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface WordApiService {
    @GET("/getMultipleRandom?count=1")
    @Headers(
        "X-RapidAPI-Key: b1837c67c7msh6ada393b433b8edp156a76jsnfccbbb2fbbe4",
        "X-RapidAPI-Host: random-words5.p.rapidapi.com"
    )
    fun getRandomWord(): Call<List<String>>
}
