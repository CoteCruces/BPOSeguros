package com.example.bposeguros.model

data class Gestion(
    val id: Long = 0,
    val siniestroId: Long,
    val fechaHora: String,
    val tipo: String,
    val descripcion: String,
    val responsable: String,
    val visible: Boolean
){
    companion object {
        const val TIPO_NOTA_LIQUIDADOR = "Gestión del liquidador"
    }
}

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
){

    val esImagen: Boolean
        get() = rutaArchivo.isNotBlank() && !nombreArchivo.endsWith(".pdf", ignoreCase = true)
}


data class Aviso(
    val id: Long = 0,
    val siniestroId: Long,
    val numeroSiniestro: String,
    val fechaHora: String,
    val motivo: String,
    val mensaje: String,
    val leido: Boolean = false
)
