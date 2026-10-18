package com.example.bposeguros.model

// Los tipos del Anexo 9 son de vehículo o de hogar
enum class LineaSiniestro { VEHICULO, HOGAR }

data class TipoSiniestro(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val linea: LineaSiniestro
)