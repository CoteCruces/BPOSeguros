package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.RecuperarClaveUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecuperarClaveViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }

    fun onEnviar() {
        val estado = _uiState.value
        val errorCorreo = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !estado.correo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")) ->
                "Ingresa un correo válido (ejemplo: usuario@dominio.com)"

            else -> null
        }
        if (estado.enviando) return
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }

        _uiState.update { it.copy(enviando = true, errorGeneral = null) }
        viewModelScope.launch {
            repositorio.recuperarClave(estado.correo)
                .onSuccess {
                    _uiState.update { it.copy(enviando = false, solicitudEnviada = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(enviando = false, errorGeneral = error.message
                            ?: "No pudimos enviar el correo. Inténtalo de nuevo en unos minutos")
                    }
                }
        }
    }
}