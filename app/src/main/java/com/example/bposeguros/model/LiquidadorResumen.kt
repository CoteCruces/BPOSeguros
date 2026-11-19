package com.example.bposeguros.model

object Equipo {
    val lista = listOf("Vehículos", "Hogar")
}

data class LiquidadorResumen(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val equipo: String,
    val activo: Boolean,
    val casosAbiertos: Int
)