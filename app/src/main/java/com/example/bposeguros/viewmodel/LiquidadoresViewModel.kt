package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.LiquidadorResumen
import com.example.bposeguros.model.LiquidadoresUiState
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class LiquidadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(LiquidadoresUiState())
    val uiState: StateFlow<LiquidadoresUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.update { it.copy(liquidadores = db.listarLiquidadores()) }
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

        val nuevoEstado = !liquidador.activo
        val filas = db.cambiarEstadoLiquidador(liquidador.id, nuevoEstado)
        val mensaje = when {
            filas == 0 -> "No se pudo cambiar el estado. Actualizamos la lista"
            nuevoEstado -> "${liquidador.nombre} fue habilitado"
            else -> "${liquidador.nombre} fue bloqueado"
        }
        _uiState.update {
            it.copy(liquidadores = db.listarLiquidadores(), pendienteDeCambio = null, mensaje = mensaje)
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }
}