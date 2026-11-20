package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.CambiarClaveUiState
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CambiarClaveViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()
    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(CambiarClaveUiState())
    val uiState: StateFlow<CambiarClaveUiState> = _uiState.asStateFlow()

    fun onActualCambia(valor: String) = _uiState.update { it.copy(actual = valor, errorActual = null) }
    fun onNuevaCambia(valor: String) = _uiState.update { it.copy(nueva = valor, errorNueva = null) }
    fun onRepetirCambia(valor: String) = _uiState.update { it.copy(repetir = valor, errorRepetir = null) }
    fun onAlternarVisibilidad() = _uiState.update { it.copy(visible = !it.visible) }

    fun onGuardar(idUsuario: Long) {
        val estado = _uiState.value
        if (estado.guardando) return

        val errorActual = if (estado.actual.isBlank()) "Escribe tu contraseña actual" else null
        val errorNueva = when {
            estado.nueva.isBlank() -> "La contraseña es obligatoria"
            estado.nueva.length < 8 -> "Debe tener al menos 8 caracteres"
            !estado.nueva.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
            !estado.nueva.any { it.isDigit() } -> "Debe incluir al menos un número"
            estado.nueva.all { it.isLetterOrDigit() } ->
                "Debe incluir al menos un carácter especial (por ejemplo @, # o !)"
            estado.nueva == estado.actual -> "La nueva contraseña debe ser distinta a la actual"
            else -> null
        }
        val errorRepetir = when {
            estado.repetir.isBlank() -> "Repite la nueva contraseña"
            estado.repetir != estado.nueva -> "Las contraseñas no coinciden"
            else -> null
        }

        if (errorActual != null || errorNueva != null || errorRepetir != null) {
            _uiState.update { it.copy(errorActual = errorActual, errorNueva = errorNueva, errorRepetir = errorRepetir) }
            return
        }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            repositorio.cambiarContrasena(idUsuario, estado.actual, estado.nueva)
                .onSuccess {
                    db.cambiarClave(idUsuario, Seguridad.huella(estado.nueva), temporal = false)
                    _uiState.value = CambiarClaveUiState(cambiada = true)
                }
                .onFailure { error ->
                    val mensaje = error.message.orEmpty()
                    _uiState.update {

                        if (mensaje.startsWith("La contraseña actual")) {
                            it.copy(guardando = false, errorActual = mensaje)
                        } else {
                            it.copy(guardando = false, errorNueva = mensaje)
                        }
                    }
                }
        }
    }
}