package com.example.bposeguros.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bposeguros.model.RecuperarClaveUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class RecuperarClaveViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) {
        _uiState.update {
            it.copy(
                rut = valor,
                errorRut = null,
                claveTemporal = null
            )
        }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor.trim(),
                errorCorreo = null,
                claveTemporal = null
            )
        }
    }

    fun onEnviar() {
        val estado = _uiState.value


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

        val hayErrores = errorRut != null || errorCorreo != null

        _uiState.update {
            it.copy(
                errorRut = errorRut,
                errorCorreo = errorCorreo,
                claveTemporal = if (hayErrores) null else generarClaveTemporal()
            )
        }
    }

    private fun generarClaveTemporal(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#"
        return (1..8)
            .map { caracteres[Random.nextInt(caracteres.length)] }
            .joinToString("")
    }

    private fun rutValido(valor: String): Boolean {
        val limpio = valor.replace(".", "").replace("-", "").trim().uppercase()
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