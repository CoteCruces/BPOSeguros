package com.example.bposeguros.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listar(@Query("rol") rol: Long?, @Query("buscar") buscar: String?): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios/asegurados")
    suspend fun crearAsegurado(@Body datos: RegistroAseguradoRequest): UsuarioResponse

    @POST("api/usuarios/liquidadores")
    suspend fun crearLiquidador(@Body datos: CrearLiquidadorRequest): UsuarioResponse

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioResponse

    @POST("api/usuarios/recuperar-clave")
    suspend fun recuperarClave(@Body datos: RecuperarClaveRequest): RespuestaDto

    @PUT("api/usuarios/{id}/perfil")
    suspend fun actualizarPerfil(@Path("id") id: Long, @Body datos: ActualizarPerfilRequest): UsuarioResponse

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Long, @Body datos: CambiarContrasenaRequest)

    @PUT("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body datos: CambiarEstadoRequest): UsuarioResponse
}