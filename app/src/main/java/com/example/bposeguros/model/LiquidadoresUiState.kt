package com.example.bposeguros.model

import com.example.bposeguros.data.remote.HistorialResponse

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
    val liquidadores: List<LiquidadorResumen> = emptyList(),   // ya filtrados por el servidor
    val busqueda: String = "",
    val visibles: Int = 3,                                     // cuántos se muestran (de 3 en 3)
    val cargando: Boolean = false,
    val error: String? = null,
    val pendienteDeCambio: LiquidadorResumen? = null,
    val liquidadorConCasos: LiquidadorResumen? = null,         // el que tiene abierta la ventana de casos
    val casosDelLiquidador: List<HistorialResponse> = emptyList(),
    val cargandoCasos: Boolean = false,
    val errorCasos: String? = null,
    val mensaje: String? = null
)