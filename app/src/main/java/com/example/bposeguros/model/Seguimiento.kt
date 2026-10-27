package com.example.bposeguros.model

data class Gestion(
    val id: Long = 0,
    val siniestroId: Long,
    val fechaHora: String,
    val tipo: String,
    val descripcion: String,
    val responsable: String,
    val visible: Boolean
)

data class SolicitudDocumento(
    val id: Long = 0,
    val siniestroId: Long,
    val tipoDocumento: String,
    val comentario: String = "",
    val fechaSolicitud: String,
    val entregado: Boolean = false,
    val fechaEntrega: String? = null
)

data class DocumentoAdjunto(
    val id: Long = 0,
    val siniestroId: Long,
    val nombreArchivo: String,
    val rutaArchivo: String,
    val tipoDocumento: String,
    val fechaCarga: String,
    val cargadoPor: String,
    val comentario: String = "",
    val solicitudId: Long? = null
)

data class Aviso(
    val id: Long = 0,
    val siniestroId: Long,
    val numeroSiniestro: String,
    val fechaHora: String,
    val motivo: String,
    val mensaje: String,
    val leido: Boolean = false
)