package com.example.bposeguros.model

data class DetalleUiState(
    val siniestro: Siniestro? = null,
    val gestiones: List<Gestion> = emptyList(),
    val solicitudes: List<SolicitudDocumento> = emptyList(),
    val adjuntos: List<DocumentoAdjunto> = emptyList()
) {
    val pendientes: Int get() = solicitudes.count { !it.entregado }
}
