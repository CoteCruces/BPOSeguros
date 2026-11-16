package com.example.bposeguros.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteRetrofit {


    private const val URL_NOTIFICACIONES = "http://10.0.2.2:8084/"


    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val notificaciones: NotificacionesApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_NOTIFICACIONES)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificacionesApi::class.java)
    }
}