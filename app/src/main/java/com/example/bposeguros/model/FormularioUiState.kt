package com.example.bposeguros.model

import android.graphics.Bitmap


data class FormularioUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,

    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,

    val registroExitoso: Boolean = false
)

data class RecuperarClaveUiState(
    val rut: String = "",
    val correo: String = "",
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val claveTemporal: String? = null
)


data class CambiarClaveUiState(
    val actual: String = "",
    val nueva: String = "",
    val repetir: String = "",
    val visible: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val cambiada: Boolean = false
)


data class PerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val comuna: String = "",
    val comunas: List<String> = emptyList(),
    val fechaNacimiento: String = "",
    val fotoActual: String = "",
    val fotoNueva: Bitmap? = null,

    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorComuna: String? = null,
    val errorFecha: String? = null,
    val errorFoto: String? = null,

    val guardado: Boolean = false
)