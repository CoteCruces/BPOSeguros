package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.AsignacionUiState
import com.example.bposeguros.model.CandidatoLiquidador
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnalistaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(AsignacionUiState())
    val uiState: StateFlow<AsignacionUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.update { it.copy(pendientes = db.listarSiniestrosSinAsignar()) }
    }

    fun onElegirCaso(caso: Siniestro) {
        _uiState.update { it.copy(casoElegido = caso, candidatos = db.listarCandidatos(caso.id)) }
    }

    fun onCerrarDialogo() {
        _uiState.update { it.copy(casoElegido = null, candidatos = emptyList()) }
    }

    fun onAsignar(candidato: CandidatoLiquidador, analista: Usuario) {
        val caso = _uiState.value.casoElegido ?: return
        if (analista.rolId != Rol.ID_ANALISTA) return

        val asignado = db.asignarLiquidador(caso.id, candidato, analista.comoResponsable(), Fechas.ahora())
        val mensaje = if (asignado) {
            "Caso ${caso.numero} asignado a ${candidato.nombreVisible()}"
        } else {
            "Ese caso ya tenía liquidador. Actualizamos la lista"
        }
        _uiState.update {
            it.copy(
                pendientes = db.listarSiniestrosSinAsignar(),
                casoElegido = null,
                candidatos = emptyList(),
                mensaje = mensaje
            )
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiar() {
        _uiState.value = AsignacionUiState()
    }
}