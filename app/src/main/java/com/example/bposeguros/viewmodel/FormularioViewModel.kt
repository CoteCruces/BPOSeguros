package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()


    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onSugerirContrasena() {
        _uiState.update {
            it.copy(contrasena = Seguridad.claveTemporal(), contrasenaVisible = true, errorContrasena = null)
        }
    }


    fun onCrear() {
        val estado = _uiState.value
        val rutLimpio = limpiarRut(estado.rut)

        val errorRut = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !rutValido(estado.rut) -> "El RUT no es válido. Revisa el dígito verificador"
            db.buscarUsuarioPorRut(rutLimpio) != null -> "Ya existe una cuenta con ese RUT"
            else -> null
        }

        val errorCorreo = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !estado.correo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")) ->
                "Ingresa un correo válido (ejemplo: usuario@dominio.com)"
            else -> null
        }

        val errorContrasena = when {
            estado.contrasena.isBlank() -> "La contraseña es obligatoria"
            estado.contrasena.length < 8 -> "Debe tener al menos 8 caracteres"
            !estado.contrasena.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
            !estado.contrasena.any { it.isDigit() } -> "Debe incluir al menos un número"
            estado.contrasena.all { it.isLetterOrDigit() } ->
                "Debe incluir al menos un carácter especial (por ejemplo @, # o !)"
            else -> null
        }

        val hayErrores = listOf(errorRut, errorCorreo, errorContrasena).any { it != null }
        _uiState.update {
            it.copy(errorRut = errorRut, errorCorreo = errorCorreo, errorContrasena = errorContrasena)
        }
        if (hayErrores) return

        val id = db.insertarAsegurado(rutLimpio, estado.correo, Seguridad.huella(estado.contrasena))
        if (id == -1L) {
            _uiState.update { it.copy(errorRut = "No se pudo crear la cuenta. Revisa el RUT") }
            return
        }
        _uiState.update { it.copy(rut = formatearRut(rutLimpio), cuentaCreada = true) }
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
