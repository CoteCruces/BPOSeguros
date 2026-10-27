package com.example.bposeguros.model

import android.graphics.Bitmap

// Una opción del desplegable "¿Qué documento es?".
// Si responde a una solicitud del liquidador, lleva su id (así la solicitud pasa a "Entregado").
data class OpcionDocumento(
    val texto: String,
    val tipoDocumento: String,
    val solicitudId: Long? = null
)

// Formulario "Adjuntar documento" (RF-07 / HU-05)
data class AdjuntarUiState(
    val numeroSiniestro: String = "",
    val opciones: List<OpcionDocumento> = emptyList(),
    val opcionElegida: Int? = null,
    val foto: Bitmap? = null,                 // vista previa de la foto tomada o elegida
    val comentario: String = "",

    val errorTipo: String? = null,
    val errorFoto: String? = null,
    val errorGeneral: String? = null,         // caso cerrado (RN-11) o máximo de adjuntos (RN-10)
    val guardado: Boolean = false
)
