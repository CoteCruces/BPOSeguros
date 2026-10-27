package com.example.bposeguros.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_ASEGURADO = 1L
        const val ID_LIQUIDADOR = 2L
        const val ID_ANALISTA = 3L
    }
}