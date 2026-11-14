package com.example.bposeguros.model

data class AsignacionUiState(
    val pendientes: List<Siniestro> = emptyList(),
    val casoElegido: Siniestro? = null,
    val candidatos: List<CandidatoLiquidador> = emptyList(),
    val mensaje: String? = null
)