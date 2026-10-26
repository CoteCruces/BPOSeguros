package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.LoginUiState
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()



    fun onModoCambia(esLiquidador: Boolean) {
        _uiState.update { it.copy(esLiquidador = esLiquidador, errorRut = null, errorCodigo = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onCodigoCambia(valor: String) {
        _uiState.update { it.copy(clave = valor, errorCodigo = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }


    fun onIngresar(): Usuario? {
        val estado = _uiState.value
        return if (estado.esLiquidador) ingresarColaborador(estado) else ingresarAsegurado(estado)
    }

    private fun ingresarAsegurado(estado: LoginUiState): Usuario? {
        val errorFormato = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !rutValido(estado.rut) -> "El RUT no es válido. Revisa el dígito verificador"
            else -> null
        }
        if (errorFormato != null) {
            _uiState.update { it.copy(errorRut = errorFormato) }
            return null
        }

        val usuario = db.buscarAseguradoPorRut(limpiarRut(estado.rut))
        if (usuario == null) {
            _uiState.update { it.copy(errorRut = "No encontramos un asegurado con ese RUT") }
        }
        return usuario
    }

    private fun ingresarColaborador(estado: LoginUiState): Usuario? {
        val codigo = estado.clave.trim().uppercase()
        val errorFormato = when {
            codigo.isBlank() -> "El código es obligatorio"
            !Regex("^LIQ-\\d{2}$").matches(codigo) -> "El código debe tener el formato LIQ-01"
            else -> null
        }
        if (errorFormato != null) {
            _uiState.update { it.copy(errorCodigo = errorFormato) }
            return null
        }

        val usuario = db.buscarColaboradorPorCodigo(codigo)
        return when {
            usuario == null -> {
                _uiState.update { it.copy(errorCodigo = "No encontramos ese código") }
                null
            }
            !usuario.activo -> {
                _uiState.update { it.copy(errorCodigo = "Ese código no está activo") }
                null
            }
            else -> usuario
        }
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
}