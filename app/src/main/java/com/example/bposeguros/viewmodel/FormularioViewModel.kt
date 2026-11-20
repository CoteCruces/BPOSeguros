package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.FormularioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }


    fun onCrear(solicitanteId: Long) {
        val estado = _uiState.value
        if (estado.enviando) return
        val rutLimpio = limpiarRut(estado.rut)

        val errorRut = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !rutValido(estado.rut) -> "El RUT no es válido. Revisa el dígito verificador"
            else -> null
        }
        val errorCorreo = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !estado.correo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")) ->
                "Ingresa un correo válido (ejemplo: usuario@dominio.com)"
            else -> null
        }

        _uiState.update { it.copy(errorRut = errorRut, errorCorreo = errorCorreo, errorGeneral = null) }
        if (errorRut != null || errorCorreo != null) return

        _uiState.update { it.copy(enviando = true) }
        viewModelScope.launch {
            repositorio.crearAsegurado(solicitanteId, rutLimpio, estado.correo)
                .onSuccess {
                    _uiState.update { it.copy(enviando = false, rut = formatearRut(rutLimpio), cuentaCreada = true) }
                }
                .onFailure { error ->
                    _uiState.update { conError(it, error.message.orEmpty()) }
                }
        }
    }


    private fun conError(estado: FormularioUiState, mensaje: String): FormularioUiState = when {
        mensaje.startsWith("Ya existe") && mensaje.contains("RUT") ->
            estado.copy(enviando = false, errorRut = mensaje)
        mensaje.startsWith("Ya existe") && mensaje.contains("correo") ->
            estado.copy(enviando = false, errorCorreo = mensaje)
        else -> estado.copy(enviando = false, errorGeneral = mensaje)
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }

    private fun limpiarRut(valor: String): String =
        valor.replace(".", "").replace("-", "").trim().uppercase()

    private fun rutValido(valor: String): Boolean {
        val limpio = limpiarRut(valor)
        if (limpio.length < 8 || limpio.length > 9) return false
        val cuerpo = limpio.dropLast(1)
        val digitoVerificador = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return digitoVerificador == esperado
    }

    private fun formatearRut(rut: String): String {
        if (rut.length < 2) return rut
        val cuerpo = rut.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${rut.last()}"
    }
}