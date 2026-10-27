package com.example.bposeguros.model

enum class DialogoLiquidador { GESTION, SOLICITUD, CIERRE }

data class AccionesUiState(
    val dialogo: DialogoLiquidador? = null,

    val descripcionGestion: String = "",
    val gestionVisible: Boolean = true,
    val errorGestion: String? = null,

    val documentoElegido: Int? = null,
    val comentarioSolicitud: String = "",
    val errorDocumento: String? = null,

    val resultadoElegido: Int? = null,
    val mensajeCierre: String = "",
    val errorResultado: String? = null,
    val errorMensaje: String? = null,

    val mensaje: String? = null
)
