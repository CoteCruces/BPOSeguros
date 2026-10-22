package com.example.bposeguros.model

data class Siniestro(
    val id: Long = 0,
    val numero: String,
    val aseguradoId: Long,
    val aseguradoNombre: String = "",
    val companiaId: Long,
    val companiaNombre: String = "",
    val tipoId: Long,
    val tipoNombre: String = "",
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val comuna: String,
    val descripcion: String = "",
    val etapa: Etapa = Etapa.RECIBIDO,
    val fechaCambioEtapa: String,
    val liquidadorId: Long? = null,
    val liquidadorNombre: String? = null,
    val liquidadorEquipo: String? = null,
    val fueraDePlazo: Boolean = false,
    val resultado: Resultado? = null,
    val mensajeCierre: String? = null,
    val ultimaActualizacion: String? = null
) {
    // RN-14: al asegurado solo se le muestra "Paulina F. – Equipo Vehículos" (nunca correo ni teléfono).
    // Supone el formato "Nombre Apellido Apellido" de los datos de ejemplo.
    fun liquidadorVisible(): String? {
        val nombre = liquidadorNombre ?: return null
        val partes = nombre.trim().split(" ")
        val abreviado = if (partes.size >= 2) "${partes[0]} ${partes[1].first()}." else partes[0]
        return if (liquidadorEquipo.isNullOrBlank()) abreviado else "$abreviado – Equipo $liquidadorEquipo"
    }
}