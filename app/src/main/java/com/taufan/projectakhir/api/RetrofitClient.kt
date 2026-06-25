package com.taufan.projectakhir.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Menggunakan URL ngrok aktif yang terdeteksi
    const val BASE_URL = "https://fragment-blog-eggbeater.ngrok-free.dev/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor khusus untuk melewati halaman peringatan browser dari ngrok
    private val ngrokInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeader = originalRequest.newBuilder()
            .header("ngrok-skip-browser-warning", "true")
            .build()
        chain.proceed(requestWithHeader)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(ngrokInterceptor) // Melewati warning page ngrok
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient() // Membuat GSON toleran jika server mengirim HTML/teks biasa
        .create()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        retrofit.create(ApiService::class.java)
    }
}
