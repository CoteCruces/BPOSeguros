package com.example.bposeguros.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface SiniestrosApi {


    @GET("api/liquidadores/casos-abiertos")
    suspend fun casosAbiertos(): List<ConteoCasosResponse>


    @GET("api/liquidadores/{id}/casos-activos")
    suspend fun casosActivos(@Path("id") id: Long): List<HistorialResponse>


    @POST("api/liquidadores/{id}/liberar-casos")
    suspend fun liberarCasos(@Path("id") id: Long, @Body datos: SolicitanteRequest): List<SiniestroRespuesta>

    @GET("api/siniestros")
    suspend fun siniestrosPorReasignar(@Query("porReasignar") porReasignar: Boolean): List<SiniestroRespuesta>

    @GET("api/siniestros/{id}/candidatos")
    suspend fun candidatos(@Path("id") id: Long): List<LiquidadorPropuesto>

    @PUT("api/siniestros/{id}/asignacion")
    suspend fun asignar(@Path("id") id: Long, @Body datos: AsignarSiniestroRequest): SiniestroRespuesta
}