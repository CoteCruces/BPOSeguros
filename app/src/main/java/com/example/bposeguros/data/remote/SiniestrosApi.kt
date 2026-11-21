package com.example.bposeguros.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SiniestrosApi {


    @GET("api/liquidadores/casos-abiertos")
    suspend fun casosAbiertos(): List<ConteoCasosResponse>


    @GET("api/liquidadores/{id}/casos-activos")
    suspend fun casosActivos(@Path("id") id: Long): List<HistorialResponse>


    @POST("api/liquidadores/{id}/liberar-casos")
    suspend fun liberarCasos(@Path("id") id: Long, @Body datos: SolicitanteRequest): List<SiniestroRespuesta>
}