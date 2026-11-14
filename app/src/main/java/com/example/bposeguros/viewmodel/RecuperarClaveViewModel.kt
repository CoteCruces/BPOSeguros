package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.EnvioCorreo
import com.example.bposeguros.data.MotivoClave
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.RecuperarClaveUiState
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarClaveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

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
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }

        val usuario = db.buscarUsuarioPorCorreo(estado.correo)
        if (usuario != null && usuario.activo) {
            val temporal = Seguridad.claveTemporal()
            val enviado = EnvioCorreo.enviarClaveTemporal(
                getApplication(), usuario.correo, temporal, MotivoClave.RECUPERACION
            )
            if (!enviado) {
                _uiState.update { it.copy(errorGeneral = "No pudimos enviar el correo. Inténtalo de nuevo en unos minutos") }
                return
            }

            db.cambiarClave(usuario.id, Seguridad.huella(temporal), temporal = true)
        }


        _uiState.update { it.copy(solicitudEnviada = true, errorGeneral = null) }
    }
}