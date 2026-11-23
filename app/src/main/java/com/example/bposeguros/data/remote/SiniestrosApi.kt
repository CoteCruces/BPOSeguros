package com.example.bposeguros.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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


    @GET("api/siniestros")
    suspend fun listar(
        @Query("aseguradoId") aseguradoId: Long? = null,
        @Query("liquidadorId") liquidadorId: Long? = null,
        @Query("sinAsignar") sinAsignar: Boolean? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("buscar") buscar: String? = null,
        @Query("orden") orden: String? = null,
        @Query("limite") limite: Int? = null
    ): List<SiniestroRespuesta>


    @GET("api/siniestros/{id}")
    suspend fun obtener(
        @Path("id") id: Long,
        @Query("aseguradoId") aseguradoId: Long? = null,
        @Query("liquidadorId") liquidadorId: Long? = null
    ): SiniestroRespuesta

    @POST("api/siniestros")
    suspend fun reportar(@Body datos: ReportarSiniestroRequest): SiniestroRespuesta

    @PUT("api/siniestros/{id}/etapa")
    suspend fun avanzarEtapa(@Path("id") id: Long, @Body datos: AvanzarEtapaRequest): SiniestroRespuesta

    @PUT("api/siniestros/{id}/cierre")
    suspend fun cerrar(@Path("id") id: Long, @Body datos: CerrarSiniestroRequest): SiniestroRespuesta


    @GET("api/siniestros/{id}/gestiones")
    suspend fun gestiones(@Path("id") id: Long, @Query("soloVisibles") soloVisibles: Boolean): List<GestionResponse>

    @POST("api/siniestros/{id}/gestiones")
    suspend fun crearGestion(@Path("id") id: Long, @Body datos: NuevaGestionRequest): GestionResponse


    @GET("api/siniestros/{id}/solicitudes")
    suspend fun solicitudes(@Path("id") id: Long): List<SolicitudResponse>

    @POST("api/siniestros/{id}/solicitudes")
    suspend fun solicitarDocumento(@Path("id") id: Long, @Body datos: NuevaSolicitudRequest): SolicitudResponse

    @GET("api/siniestros/{id}/adjuntos")
    suspend fun adjuntos(@Path("id") id: Long): List<AdjuntoRespuesta>


    @Multipart
    @POST("api/siniestros/{id}/adjuntos")
    suspend fun subirAdjunto(
        @Path("id") id: Long,
        @Part archivo: MultipartBody.Part,
        @Part("tipoDocumento") tipoDocumento: RequestBody,
        @Part("comentario") comentario: RequestBody,
        @Part("cargadoPor") cargadoPor: RequestBody,
        @Part("solicitudId") solicitudId: RequestBody?
    ): AdjuntoRespuesta


    @GET("api/avisos")
    suspend fun avisos(@Query("aseguradoId") aseguradoId: Long): List<AvisoRespuesta>

    @PUT("api/avisos/{id}/leido")
    suspend fun marcarAvisoLeido(@Path("id") id: Long)
}