package com.example.myprojectcitypulse.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://overpass.kumi.systems/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->

            val request = chain.request()
                .newBuilder()
                .header("User-Agent", "CityPulse")
                .header("Accept", "*/*")
                .build()

            chain.proceed(request)
        }
        .build()

    val apiService: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}