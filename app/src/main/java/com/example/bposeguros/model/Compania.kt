package com.example.bposeguros.model

data class Compania(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val lineas: List<LineaSiniestro>,
    val plazoDiasHabiles: Int,
    val activa: Boolean = true
)