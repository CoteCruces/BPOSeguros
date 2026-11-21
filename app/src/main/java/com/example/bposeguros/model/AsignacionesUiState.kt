package com.example.bposeguros.model

import com.example.bposeguros.data.remote.LiquidadorPropuesto
import com.example.bposeguros.data.remote.SiniestroRespuesta

data class AsignacionUiState(
    val pendientes: List<Siniestro> = emptyList(),
    val casoElegido: Siniestro? = null,
    val candidatos: List<CandidatoLiquidador> = emptyList(),
    val mensaje: String? = null,

    val porReasignar: List<SiniestroRespuesta> = emptyList(),
    val errorPorReasignar: String? = null,
    val casoAReasignar: SiniestroRespuesta? = null,
    val candidatosReasignar: List<LiquidadorPropuesto> = emptyList(),
    val cargandoCandidatos: Boolean = false,
    val errorReasignar: String? = null
)