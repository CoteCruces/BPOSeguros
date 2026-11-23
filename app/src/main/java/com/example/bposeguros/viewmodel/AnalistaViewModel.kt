package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.ReasignacionRepository
import com.example.bposeguros.data.SiniestroRepository
import com.example.bposeguros.data.remote.LiquidadorPropuesto
import com.example.bposeguros.data.remote.SiniestroRespuesta
import com.example.bposeguros.data.remote.aCandidato
import com.example.bposeguros.model.AsignacionUiState
import com.example.bposeguros.model.CandidatoLiquidador
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalistaViewModel(application: Application) : AndroidViewModel(application) {


    private val siniestros = SiniestroRepository()
    private val reasignacion = ReasignacionRepository()

    private val _uiState = MutableStateFlow(AsignacionUiState())
    val uiState: StateFlow<AsignacionUiState> = _uiState.asStateFlow()


    fun cargar() {
        cargarPendientes()
        cargarPorReasignar()
    }

    private fun cargarPendientes() {
        viewModelScope.launch {
            siniestros.sinAsignar()
                .onSuccess { lista -> _uiState.update { it.copy(pendientes = lista) } }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message) } }
        }
    }

    private fun cargarPorReasignar() {
        viewModelScope.launch {
            reasignacion.casosPorReasignar()
                .onSuccess { casos -> _uiState.update { it.copy(porReasignar = casos, errorPorReasignar = null) } }
                .onFailure { e -> _uiState.update { it.copy(errorPorReasignar = e.message) } }
        }
    }


    fun onElegirCaso(caso: Siniestro) {
        viewModelScope.launch {
            reasignacion.candidatos(caso.id)
                .onSuccess { lista ->
                    _uiState.update { it.copy(casoElegido = caso, candidatos = lista.map { c -> c.aCandidato() }) }
                }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message) } }
        }
    }

    fun onCerrarDialogo() {
        _uiState.update { it.copy(casoElegido = null, candidatos = emptyList()) }
    }

    fun onAsignar(candidato: CandidatoLiquidador, analista: Usuario) {
        val caso = _uiState.value.casoElegido ?: return
        if (analista.rolId != Rol.ID_ANALISTA) return

        viewModelScope.launch {
            reasignacion.reasignar(caso.id, analista.id, candidato.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            casoElegido = null, candidatos = emptyList(),
                            mensaje = "Caso ${caso.numero} asignado a ${candidato.nombreVisible()}"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(casoElegido = null, candidatos = emptyList(), mensaje = e.message) }
                }
            cargarPendientes()
        }
    }


    fun onReasignar(caso: SiniestroRespuesta) {
        _uiState.update {
            it.copy(
                casoAReasignar = caso, candidatosReasignar = emptyList(),
                cargandoCandidatos = true, errorReasignar = null
            )
        }
        viewModelScope.launch {
            reasignacion.candidatos(caso.id)
                .onSuccess { lista -> _uiState.update { it.copy(candidatosReasignar = lista, cargandoCandidatos = false) } }
                .onFailure { e -> _uiState.update { it.copy(cargandoCandidatos = false, errorReasignar = e.message) } }
        }
    }

    fun onCerrarReasignar() {
        _uiState.update { it.copy(casoAReasignar = null, candidatosReasignar = emptyList(), errorReasignar = null) }
    }

    fun onConfirmarReasignar(candidato: LiquidadorPropuesto, analista: Usuario) {
        val caso = _uiState.value.casoAReasignar ?: return
        if (analista.rolId != Rol.ID_ANALISTA) return

        _uiState.update { it.copy(cargandoCandidatos = true, errorReasignar = null) }
        viewModelScope.launch {
            reasignacion.reasignar(caso.id, analista.id, candidato.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            casoAReasignar = null,
                            candidatosReasignar = emptyList(),
                            cargandoCandidatos = false,
                            mensaje = "Caso ${caso.numero} reasignado a ${candidato.nombre}"
                        )
                    }
                    cargarPorReasignar()
                }
                .onFailure { e -> _uiState.update { it.copy(cargandoCandidatos = false, errorReasignar = e.message) } }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiar() {
        _uiState.value = AsignacionUiState()
    }
}