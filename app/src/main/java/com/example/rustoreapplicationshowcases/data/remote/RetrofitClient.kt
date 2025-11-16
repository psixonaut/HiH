package com.example.rustoreapplicationshowcases.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Используем бесплатный хостинг для статики, например GitHub Pages или JSONPlaceholder
    // Для демонстрации можно использовать: https://jsonplaceholder.typicode.com
    // Или создать свой JSON файл на GitHub Pages
    // Пример URL: https://raw.githubusercontent.com/username/repo/main/apps.json
    private const val BASE_URL = "https://raw.githubusercontent.com/"
    
    // Альтернативный вариант - использовать JSONPlaceholder или другой бесплатный сервис
    // Для демонстрации можно использовать локальный сервер или GitHub Gist
    // Пример: https://gist.githubusercontent.com/username/gist-id/raw/apps.json
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // Метод для установки кастомного URL (для режима администратора)
    fun createApiService(baseUrl: String): ApiService {
        val customRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return customRetrofit.create(ApiService::class.java)
    }
}

