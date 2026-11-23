package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.SiniestroRepository
import com.example.bposeguros.data.avisoCorto
import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.Siniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AseguradoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = SiniestroRepository()

    private val _ultimos = MutableStateFlow<List<Siniestro>>(emptyList())
    val ultimos: StateFlow<List<Siniestro>> = _ultimos.asStateFlow()

    private val _resultados = MutableStateFlow<List<Siniestro>>(emptyList())
    val resultados: StateFlow<List<Siniestro>> = _resultados.asStateFlow()

    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()

    private fun avisarError(error: Throwable) {
        getApplication<Application>().avisoCorto(error.message.orEmpty())
    }

    fun cargar(idAsegurado: Long) {
        viewModelScope.launch {
            repositorio.ultimosSiniestros(idAsegurado, cantidad = 2)
                .onSuccess { _ultimos.value = it }
                .onFailure { avisarError(it) }
            repositorio.avisos(idAsegurado).onSuccess { _avisos.value = it }
        }
    }

    fun buscar(idAsegurado: Long, desde: String, hasta: String) {
        viewModelScope.launch {
            val resultado =
                if (desde.isBlank() || hasta.isBlank()) repositorio.siniestrosDe(idAsegurado)
                else repositorio.siniestrosEntre(idAsegurado, desde, hasta)
            resultado
                .onSuccess { _resultados.value = it }
                .onFailure { avisarError(it) }
        }
    }

    fun cargarDetalle(idSiniestro: Long, idAsegurado: Long) {
        viewModelScope.launch {
            // soloVisibles = true: el asegurado NO ve las notas internas del liquidador
            repositorio.detalle(idSiniestro, aseguradoId = idAsegurado, liquidadorId = null, soloVisibles = true)
                .onSuccess { _detalle.value = it }
                .onFailure {
                    _detalle.value = DetalleUiState()
                    avisarError(it)
                }
        }
    }

    fun marcarLeido(aviso: Aviso, idAsegurado: Long) {
        viewModelScope.launch {
            if (!aviso.leido) repositorio.marcarAvisoLeido(aviso.id)
            repositorio.avisos(idAsegurado).onSuccess { _avisos.value = it }
        }
    }
}