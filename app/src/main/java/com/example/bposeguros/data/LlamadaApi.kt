package com.example.bposeguros.data

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


class ErrorApi(mensaje: String) : Exception(mensaje)

private const val SIN_CONEXION = "No hay conexión con el servidor. Revisa que los microservicios estén encendidos"


private data class CuerpoError(val message: String?, val detail: String?)


suspend fun <T> llamarApi(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: HttpException) {
        Result.failure(ErrorApi(mensajeDelServidor(e)))
    } catch (e: SocketTimeoutException) {
        Result.failure(ErrorApi("El servidor tardó demasiado en responder. Revisa si la acción se realizó antes de volver a intentarlo"))
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