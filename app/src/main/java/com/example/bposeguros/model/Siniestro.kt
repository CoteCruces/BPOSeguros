package com.example.bposeguros.model

import com.example.bposeguros.R

data class Siniestro(
    val id: Long = 0,
    val numero: String,
    val aseguradoId: Long,
    val aseguradoNombre: String = "",
    val aseguradoRut: String = "",            // para que el liquidador reconozca al cliente
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
    val estaCerrado: Boolean get() = etapa == Etapa.CERRADO

    // RN-14: al asegurado solo se le muestra "Paulina F. – Equipo Vehículos" (nunca correo ni teléfono).
    // Supone el formato "Nombre Apellido Apellido" de los datos de ejemplo.
    fun liquidadorVisible(): String? {
        val nombre = liquidadorNombre ?: return null
        val partes = nombre.trim().split(" ")
        val abreviado = if (partes.size >= 2) "${partes[0]} ${partes[1].first()}." else partes[0]
        return if (liquidadorEquipo.isNullOrBlank()) abreviado else "$abreviado – Equipo $liquidadorEquipo"
    }

    // El RUT del cliente con puntos y guion, para la lista del liquidador: "158423715" -> "15.842.371-5"
    fun rutAseguradoFormateado(): String {
        if (aseguradoRut.length < 2) return aseguradoRut
        val cuerpo = aseguradoRut.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${aseguradoRut.last()}"
    }

    // "¿Qué viene ahora?": el texto depende de la etapa y de si faltan documentos
    fun proximoPaso(hayPendientes: Boolean): Int = when (etapa) {
        Etapa.RECIBIDO -> R.string.paso_recibido
        Etapa.EN_EVALUACION ->
            if (hayPendientes) R.string.paso_evaluacion_pendientes else R.string.paso_evaluacion_sin_pendientes
        Etapa.EN_LIQUIDACION -> R.string.paso_liquidacion
        Etapa.CERRADO -> R.string.paso_cerrado
    }
}
