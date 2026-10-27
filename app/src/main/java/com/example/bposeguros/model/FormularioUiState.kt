package com.example.bposeguros.model

import android.graphics.Bitmap

// =====================================================================
//  FormularioUiState.kt  —  Estados de las pantallas de la CUENTA
//  (crear cliente, recuperar contraseña, cambiar contraseña y mi perfil).
//  null = sin error. Todas son inmutables: se cambian con .copy(...)
// =====================================================================

// "Nuevo cliente" (lo usa el liquidador). Es el antiguo formulario de registro,
// que se movió de la pantalla de inicio a la vista del liquidador.
// Obligatorio: RUT, correo y una contraseña temporal. Lo demás lo completa el cliente.
data class FormularioUiState(
    val rut: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,

    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,

    val cuentaCreada: Boolean = false
)

// "¿Olvidaste tu contraseña?": RUT + correo de la cuenta
data class RecuperarClaveUiState(
    val rut: String = "",
    val correo: String = "",
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val claveTemporal: String? = null      // != null -> se muestra el correo (simulado) enviado
)

// "Cambiar contraseña": la actual + la nueva dos veces
data class CambiarClaveUiState(
    val actual: String = "",
    val nueva: String = "",
    val repetir: String = "",
    val visible: Boolean = false,          // un solo "ojo" para los tres campos
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val cambiada: Boolean = false
)

// "Mi perfil": todo se puede cambiar menos el RUT
data class PerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val comuna: String = "",               // "" = sin indicar
    val comunas: List<String> = emptyList(),   // la lista viene de la BD (tabla comunas)
    val fechaNacimiento: String = "",      // como la escribe la persona: dd-mm-aaaa
    val fotoActual: String = "",           // ruta guardada en la BD ("" = avatar por defecto)
    val fotoNueva: Bitmap? = null,         // foto recién tomada o elegida (aún sin guardar)

    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorComuna: String? = null,
    val errorFecha: String? = null,
    val errorFoto: String? = null,

    val guardado: Boolean = false
)
