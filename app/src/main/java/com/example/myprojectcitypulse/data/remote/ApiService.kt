package com.example.myprojectcitypulse.data.remote

import ResponseApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("interpreter")
    suspend fun getLieux(
        @Query("data") query:String): Response<ResponseApi>

}