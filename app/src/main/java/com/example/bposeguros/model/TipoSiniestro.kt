package com.example.bposeguros.model


enum class LineaSiniestro { VEHICULO, HOGAR }

data class TipoSiniestro(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val linea: LineaSiniestro
)