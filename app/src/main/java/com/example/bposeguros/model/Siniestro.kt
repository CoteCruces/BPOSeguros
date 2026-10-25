package com.example.bposeguros.model

import com.example.bposeguros.R

data class Siniestro(
    val id: Long = 0,
    val numero: String,
    val aseguradoId: Long,
    val aseguradoNombre: String = "",
    val aseguradoRut: String = "",

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
    fun liquidadorVisible(): String? {
        val nombre = liquidadorNombre ?: return null
        val partes = nombre.trim().split(" ")
        val abreviado = if (partes.size >= 2) "${partes[0]} ${partes[1].first()}." else partes[0]
        return if (liquidadorEquipo.isNullOrBlank()) abreviado else "$abreviado – Equipo $liquidadorEquipo"
    }

    val estaCerrado: Boolean get() = etapa == Etapa.CERRADO

    fun proximoPaso(hayPendientes: Boolean): Int = when (etapa) {
        Etapa.RECIBIDO -> R.string.paso_recibido
        Etapa.EN_EVALUACION ->
            if (hayPendientes) R.string.paso_evaluacion_pendientes else R.string.paso_evaluacion_sin_pendientes
        Etapa.EN_LIQUIDACION -> R.string.paso_liquidacion
        Etapa.CERRADO -> R.string.paso_cerrado
    }
}