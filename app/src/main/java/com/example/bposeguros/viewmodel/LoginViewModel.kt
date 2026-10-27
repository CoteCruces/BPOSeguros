package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.LoginUiState
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()


    fun onModoCambia(esLiquidador: Boolean) {
        _uiState.update { it.copy(esLiquidador = esLiquidador, errorRut = null, errorClave = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }


    fun onIngresar(): Usuario? {
        val estado = _uiState.value

        val errorRut = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !rutValido(estado.rut) -> "El RUT no es válido. Revisa el dígito verificador"
            else -> null
        }
        val errorClave = if (estado.clave.isBlank()) "Escribe tu contraseña" else null
        if (errorRut != null || errorClave != null) {
            _uiState.update { it.copy(errorRut = errorRut, errorClave = errorClave) }
            return null
        }

        val usuario = db.buscarPorRutYClave(limpiarRut(estado.rut), Seguridad.huella(estado.clave))

        val error = when {
            usuario == null -> "RUT o contraseña incorrectos"
            !usuario.activo -> "Tu cuenta no está activa. Comunícate con Andes"
            usuario.rolId == Rol.ID_ANALISTA -> "Por ahora solo asegurados y liquidadores usan la app"
            estado.esLiquidador && usuario.esAsegurado ->
                "Esta cuenta es de cliente. Desactiva \"Soy liquidador de Andes\""
            !estado.esLiquidador && !usuario.esAsegurado ->
                "Esta cuenta es de liquidador. Activa \"Soy liquidador de Andes\""
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorClave = error) }
            return null
        }
        _uiState.value = LoginUiState(esLiquidador = estado.esLiquidador)
        return usuario
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
