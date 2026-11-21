package com.example.bposeguros.data

import com.example.bposeguros.data.remote.CambiarEstadoRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import com.example.bposeguros.data.remote.HistorialResponse
import com.example.bposeguros.data.remote.SiniestrosApi
import com.example.bposeguros.data.remote.SolicitanteRequest
import com.example.bposeguros.data.remote.UsuarioApi
import com.example.bposeguros.model.LiquidadorResumen

private const val ROL_LIQUIDADOR = 2L


class LiquidadorRepository(
    private val usuarios: UsuarioApi = ClienteRetrofit.usuarios,
    private val siniestros: SiniestrosApi = ClienteRetrofit.siniestros
) {


    suspend fun listar(buscar: String): Result<List<LiquidadorResumen>> = llamarApi {
        val filtro = buscar.trim().ifEmpty { null }
        val liquidadores = usuarios.listar(ROL_LIQUIDADOR, filtro)
        val abiertos = siniestros.casosAbiertos().associate { it.liquidadorId to it.casosAbiertos.toInt() }
        liquidadores.map {
            LiquidadorResumen(it.id, it.nombre, it.codigo, it.equipo, it.activo, abiertos[it.id] ?: 0)
        }
    }

    suspend fun casosActivos(liquidadorId: Long): Result<List<HistorialResponse>> =
        llamarApi { siniestros.casosActivos(liquidadorId) }


    suspend fun bloquear(liquidadorId: Long, analistaId: Long): Result<Unit> = llamarApi {
        siniestros.liberarCasos(liquidadorId, SolicitanteRequest(analistaId))
        usuarios.cambiarEstado(liquidadorId, CambiarEstadoRequest(analistaId, false))
        Unit
    }

    suspend fun habilitar(liquidadorId: Long, analistaId: Long): Result<Unit> = llamarApi {
        usuarios.cambiarEstado(liquidadorId, CambiarEstadoRequest(analistaId, true))
        Unit
    }
}