package com.example.rustoreapplicationshowcases.data.remote

import com.example.rustoreapplicationshowcases.data.model.AppInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getApps(@Url url: String? = null): Response<List<AppInfo>>
}

