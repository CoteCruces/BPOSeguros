package com.example.bposeguros.model

data class NuevoLiquidadorUiState(
    val rut: String = "",
    val nombre: String = "",
    val correo: String = "",
    val equipoElegido: Int? = null,

    val errorRut: String? = null,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorEquipo: String? = null,
    val errorGeneral: String? = null,

    val enviando: Boolean = false,
    val liquidadorCreado: Boolean = false,
    val codigoCreado: String = ""
)

data class LiquidadoresUiState(
    val liquidadores: List<LiquidadorResumen> = emptyList(),
    val pendienteDeCambio: LiquidadorResumen? = null,
    val mensaje: String? = null
)