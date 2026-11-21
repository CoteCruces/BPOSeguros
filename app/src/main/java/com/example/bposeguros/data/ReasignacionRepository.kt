package com.example.bposeguros.data

import com.example.bposeguros.data.remote.AsignarSiniestroRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import com.example.bposeguros.data.remote.LiquidadorPropuesto
import com.example.bposeguros.data.remote.SiniestroRespuesta
import com.example.bposeguros.data.remote.SiniestrosApi

class ReasignacionRepository(
    private val api: SiniestrosApi = ClienteRetrofit.siniestros
) {

    suspend fun casosPorReasignar(): Result<List<SiniestroRespuesta>> =
        llamarApi { api.siniestrosPorReasignar(true) }

    suspend fun candidatos(idSiniestro: Long): Result<List<LiquidadorPropuesto>> =
        llamarApi { api.candidatos(idSiniestro) }

    suspend fun reasignar(idSiniestro: Long, analistaId: Long, liquidadorId: Long): Result<SiniestroRespuesta> =
        llamarApi { api.asignar(idSiniestro, AsignarSiniestroRequest(analistaId, liquidadorId)) }
}