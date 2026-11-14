package com.example.bposeguros.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

enum class MotivoClave { CREACION, RECUPERACION }

object EnvioCorreo {

    private const val ETIQUETA = "BPO_DEV_CORREO"

    fun enviarClaveTemporal(
        context: Context,
        correo: String,
        claveTemporal: String,
        motivo: MotivoClave
    ): Boolean {
        val esDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (esDebug) {
            Log.d(ETIQUETA, "[$motivo] Para: $correo | Clave temporal: $claveTemporal")
        }
        return true
    }
}