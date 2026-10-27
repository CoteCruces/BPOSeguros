package com.example.bposeguros.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {

    // NUEVO (SharedPreferences): abre (o crea) el archivo "sesion.xml".
    // MODE_PRIVATE = solo esta app puede leerlo.
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    // NUEVO (SharedPreferences): guarda el id del usuario que inició sesión.
    // edit { } abre el editor y al terminar llama a apply() (guarda en segundo plano).
    fun guardar(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    // NUEVO (SharedPreferences): lee el id guardado. Si la clave no existe devuelve -1.
    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    // NUEVO (SharedPreferences): borra la clave -> al reabrir la app no hay sesión.
    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}