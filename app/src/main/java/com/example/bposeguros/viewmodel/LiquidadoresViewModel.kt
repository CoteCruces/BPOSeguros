package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.LiquidadorRepository
import com.example.bposeguros.model.LiquidadorResumen
import com.example.bposeguros.model.LiquidadoresUiState
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGINA = 3

class LiquidadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = LiquidadorRepository()

    private val _uiState = MutableStateFlow(LiquidadoresUiState())
    val uiState: StateFlow<LiquidadoresUiState> = _uiState.asStateFlow()

    private var busquedaPendiente: Job? = null

    fun cargar() {
        viewModelScope.launch { recargar() }
    }


    fun onBusquedaCambia(texto: String) {
        _uiState.update { it.copy(busqueda = texto, visibles = PAGINA) }
        busquedaPendiente?.cancel()
        busquedaPendiente = viewModelScope.launch {
            delay(400)
            recargar()
        }
    }

    fun onVerMas() {
        _uiState.update { it.copy(visibles = it.visibles + PAGINA) }
    }

    private suspend fun recargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        repositorio.listar(_uiState.value.busqueda)
            .onSuccess { lista -> _uiState.update { it.copy(liquidadores = lista, cargando = false) } }
            .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message) } }
    }


    fun onVerCasos(liquidador: LiquidadorResumen) {
        _uiState.update {
            it.copy(liquidadorConCasos = liquidador, casosDelLiquidador = emptyList(),
                cargandoCasos = true, errorCasos = null)
        }
        viewModelScope.launch {
            repositorio.casosActivos(liquidador.id)
                .onSuccess { casos -> _uiState.update { it.copy(casosDelLiquidador = casos, cargandoCasos = false) } }
                .onFailure { e -> _uiState.update { it.copy(cargandoCasos = false, errorCasos = e.message) } }
        }
    }

    fun onCerrarCasos() {
        _uiState.update { it.copy(liquidadorConCasos = null) }
    }


    fun onPedirCambio(liquidador: LiquidadorResumen) {
        _uiState.update { it.copy(pendienteDeCambio = liquidador) }
    }

    fun onCancelarCambio() {
        _uiState.update { it.copy(pendienteDeCambio = null) }
    }

    fun onConfirmarCambio(analista: Usuario) {
        val liquidador = _uiState.value.pendienteDeCambio ?: return
        if (analista.rolId != Rol.ID_ANALISTA) return

        val bloquear = liquidador.activo
        _uiState.update { it.copy(pendienteDeCambio = null, cargando = true) }
        viewModelScope.launch {
            val resultado = if (bloquear) repositorio.bloquear(liquidador.id, analista.id)
            else repositorio.habilitar(liquidador.id, analista.id)

            resultado
                .onSuccess {
                    val texto = if (bloquear) "${liquidador.nombre} fue bloqueado" else "${liquidador.nombre} fue habilitado"
                    _uiState.update { it.copy(mensaje = texto) }
                    recargar()
                }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, mensaje = e.message) } }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }
}