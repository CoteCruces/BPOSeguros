package com.example.bposeguros.model

// Los 3 formularios cortos del liquidador (RF-10, RF-11, HU-09, HU-10, HU-11).
// Cada uno se abre en un diálogo; "dialogo" indica cuál está abierto (null = ninguno).
enum class DialogoLiquidador { GESTION, SOLICITUD, CIERRE }

data class AccionesUiState(
    val dialogo: DialogoLiquidador? = null,

    // Registrar gestión (HU-09)
    val descripcionGestion: String = "",
    val gestionVisible: Boolean = true,           // RN-13: visible para el asegurado o interna
    val errorGestion: String? = null,

    // Solicitar documento (HU-11)
    val documentoElegido: Int? = null,            // posición en TipoDocumento.lista
    val comentarioSolicitud: String = "",
    val errorDocumento: String? = null,

    // Cerrar el caso (HU-10 CA3)
    val resultadoElegido: Int? = null,            // posición en etapa.resultadosPermitidos()
    val mensajeCierre: String = "",
    val errorResultado: String? = null,
    val errorMensaje: String? = null,

    // Mensaje corto para el Snackbar ("Gestión registrada", "No se puede avanzar...")
    val mensaje: String? = null
)
