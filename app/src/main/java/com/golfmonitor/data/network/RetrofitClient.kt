package com.golfmonitor.data.network

import com.golfmonitor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val HOST = "uk-golf-course-data-api.p.rapidapi.com"
    private const val BASE_URL = "https://$HOST/"

    // Debug builds log request lines only; never headers, so the API key stays out of logcat.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        redactHeader("X-RapidAPI-Key")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("X-RapidAPI-Host", HOST).build())
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val ukGolfApiService: UkGolfApiService = retrofit.create(UkGolfApiService::class.java)
}
