package com.example.bposeguros.model

data class LoginUiState(
    val esLiquidador: Boolean = false,
    val rut: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val errorRut: String? = null,
    val errorClave: String? = null
)
