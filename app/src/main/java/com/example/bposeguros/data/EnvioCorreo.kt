package com.example.bposeguros.data

import com.example.bposeguros.data.remote.ClaveTemporalRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import retrofit2.HttpException
import java.io.IOException

enum class MotivoClave { CREACION, RECUPERACION }

object EnvioCorreo {

    suspend fun enviarClaveTemporal(
        correo: String,
        claveTemporal: String,
        motivo: MotivoClave,
        rol: String = "ASEGURADO"
    ): Boolean =
        try {
            ClienteRetrofit.notificaciones.enviarClaveTemporal(
                ClaveTemporalRequest(correo, claveTemporal, motivo.name, rol)
            )
            true
        } catch (e: IOException) {
            false
        } catch (e: HttpException) {
            false
        }
}