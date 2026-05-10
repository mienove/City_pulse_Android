package com.example.myprojectcitypulse.data.remote

import com.example.myprojectcitypulse.model.OverpassResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("interpreter")
    suspend fun getLieux(
        @Query("data") query: String
    ): Response<OverpassResponse>
}