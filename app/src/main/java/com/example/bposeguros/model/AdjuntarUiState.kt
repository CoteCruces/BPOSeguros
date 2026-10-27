package com.example.bposeguros.model

import android.graphics.Bitmap

data class OpcionDocumento(
    val texto: String,
    val tipoDocumento: String,
    val solicitudId: Long? = null
)

data class AdjuntarUiState(
    val numeroSiniestro: String = "",
    val opciones: List<OpcionDocumento> = emptyList(),
    val opcionElegida: Int? = null,
    val foto: Bitmap? = null,
    val comentario: String = "",

    val errorTipo: String? = null,
    val errorFoto: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
)
