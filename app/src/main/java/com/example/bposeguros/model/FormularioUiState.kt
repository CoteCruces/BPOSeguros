package com.example.bposeguros.model

import android.graphics.Bitmap

data class FormularioUiState(
    val rut: String = "",
    val correo: String = "",


    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorGeneral: String? = null,

    val cuentaCreada: Boolean = false,
    val enviando: Boolean = false
)

data class RecuperarClaveUiState(
    val correo: String = "",
    val errorCorreo: String? = null,
    val errorGeneral: String? = null,
    val solicitudEnviada: Boolean = false,
    val enviando: Boolean = false
)

data class CambiarClaveUiState(
    val actual: String = "",
    val nueva: String = "",
    val repetir: String = "",
    val visible: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val cambiada: Boolean = false,
    val guardando: Boolean = false
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

    val guardado: Boolean = false,
    val errorGeneral: String? = null
)
