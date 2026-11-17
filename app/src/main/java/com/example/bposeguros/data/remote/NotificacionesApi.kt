package com.example.bposeguros.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class ClaveTemporalRequest(
    val correo: String,
    val claveTemporal: String,
    val motivo: String,
    val rol: String = "ASEGURADO"
)
data class RespuestaDto(val mensaje: String)

interface NotificacionesApi {
    @POST("api/notificaciones/clave-temporal")
    suspend fun enviarClaveTemporal(@Body datos: ClaveTemporalRequest): RespuestaDto
}