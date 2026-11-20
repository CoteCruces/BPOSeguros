package com.example.bposeguros.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteRetrofit {

    private const val URL_USUARIOS = "http://10.0.2.2:8081/"

    
    private const val URL_NOTIFICACIONES = "http://10.0.2.2:8084/"
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarios: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }
    val notificaciones: NotificacionesApi by lazy { crear(URL_NOTIFICACIONES).create(NotificacionesApi::class.java) }
}
