package com.example.myprojectcitypulse.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers(
        "Content-Type: text/plain"
    )
    @POST("api/interpreter")
    suspend fun getLieux(
        @Body body: RequestBody
    ): Response<ResponseBody>
}