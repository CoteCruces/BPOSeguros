package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.RecuperarClaveUiState
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  RecuperarClaveViewModel.kt  —  "¿Olvidaste tu contraseña?"
//  1) La persona escribe su RUT y el correo de su cuenta.
//  2) Si coinciden con la BD, se crea una contraseña TEMPORAL y se guarda (su huella).
//  3) Se "envía" al correo. Mandar un correo de verdad necesita un servidor,
//     que este proyecto no tiene; por eso la pantalla muestra el correo SIMULADO.
//  4) Al entrar con la temporal, la app obliga a cambiarla (clave_temporal = 1).
// =====================================================================
class RecuperarClaveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
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
        if (errorRut != null || errorCorreo != null) {
            _uiState.update { it.copy(errorRut = errorRut, errorCorreo = errorCorreo) }
            return
        }

        // El correo debe ser EL de esa cuenta (ignoreCase = true: no importan mayúsculas)
        val usuario = db.buscarUsuarioPorRut(limpiarRut(estado.rut))
        if (usuario == null || !usuario.correo.equals(estado.correo, ignoreCase = true)) {
            _uiState.update { it.copy(errorCorreo = "El RUT y el correo no coinciden con ninguna cuenta") }
            return
        }

        val temporal = Seguridad.claveTemporal()
        db.cambiarClave(usuario.id, Seguridad.huella(temporal), temporal = true)
        _uiState.update { it.copy(correo = usuario.correo, claveTemporal = temporal) }
    }

    // ----- Validación del RUT (RN-03) -----

    // "15.842.371-5" -> "158423715"  (sin puntos, sin guion, K en mayúscula)
    private fun limpiarRut(valor: String): String =
        valor.replace(".", "").replace("-", "").trim().uppercase()

    // Algoritmo módulo 11: multiplica los dígitos de derecha a izquierda por 2,3,4,5,6,7,2,3...
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
