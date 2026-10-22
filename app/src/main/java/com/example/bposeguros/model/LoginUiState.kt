package com.example.bposeguros.model

// Estado de la pantalla de ingreso: lo escrito, el modo (asegurado/liquidador) y los errores.
// null = sin error. Es inmutable: para cambiarlo se crea una copia con .copy(...)
data class LoginUiState(
    val esColaborador: Boolean = false,
    val rut: String = "",
    val codigo: String = "",
    val errorRut: String? = null,
    val errorCodigo: String? = null
)