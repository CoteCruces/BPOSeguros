package com.example.bposeguros.data

import com.example.bposeguros.data.remote.CambiarContrasenaRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import com.example.bposeguros.data.remote.CrearLiquidadorRequest
import com.example.bposeguros.data.remote.LoginRequest
import com.example.bposeguros.data.remote.RegistroAseguradoRequest
import com.example.bposeguros.data.remote.UsuarioApi
import com.example.bposeguros.data.remote.aUsuario
import com.example.bposeguros.model.Usuario
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException


class ErrorApi(mensaje: String) : Exception(mensaje)

private const val SIN_CONEXION = "No hay conexión con el servidor. Revisa que los microservicios estén encendidos"


private data class CuerpoError(val message: String?, val detail: String?)


class UsuarioRepository(
    private val api: UsuarioApi = ClienteRetrofit.usuarios
) {

    suspend fun iniciarSesion(rut: String, contrasena: String): Result<Usuario> =
        llamar { api.iniciarSesion(LoginRequest(rut, contrasena)).aUsuario() }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String): Result<Unit> =
        llamar { api.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun obtener(id: Long): Result<Usuario> =
        llamar { api.obtener(id).aUsuario() }

    suspend fun crearAsegurado(solicitanteId: Long, rut: String, correo: String): Result<Usuario> =
        llamar { api.crearAsegurado(RegistroAseguradoRequest(solicitanteId, rut, correo)).aUsuario() }

    suspend fun crearLiquidador(
        solicitanteId: Long, rut: String, nombre: String, correo: String, equipo: String
    ): Result<Usuario> =
        llamar { api.crearLiquidador(
            CrearLiquidadorRequest(
                solicitanteId,
                rut,
                nombre,
                correo,
                equipo
            )
        ).aUsuario() }


    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: HttpException) {
            Result.failure(ErrorApi(mensajeDelServidor(e)))
        } catch (e: IOException) {
            Result.failure(ErrorApi(SIN_CONEXION))
        }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = runCatching {
            Gson().fromJson(cuerpo, CuerpoError::class.java)?.let { it.message ?: it.detail }
        }.getOrNull()
        return when {

            !mensaje.isNullOrBlank() -> mensaje

            e.code() in listOf(404, 502, 503, 504) -> SIN_CONEXION
            else -> "Ocurrió un error inesperado (${e.code()})"
        }
    }
}