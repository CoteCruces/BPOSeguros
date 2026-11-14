package com.example.bposeguros.model

data class CandidatoLiquidador(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val equipo: String,
    val casosAbiertos: Int
) {
    fun nombreVisible(): String {
        val partes = nombre.trim().split(" ")
        val abreviado = if (partes.size >= 2) "${partes[0]} ${partes[1].first()}." else partes[0]
        return if (equipo.isBlank()) abreviado else "$abreviado – Equipo $equipo"
    }
}