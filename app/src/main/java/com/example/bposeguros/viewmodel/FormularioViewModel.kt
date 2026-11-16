package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.EnvioCorreo
import com.example.bposeguros.data.MotivoClave
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.ui.util.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()


    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
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
            db.correoEnUso(estado.correo) -> "Ya existe una cuenta con ese correo"
            else -> null
        }

        _uiState.update { it.copy(errorRut = errorRut, errorCorreo = errorCorreo, errorGeneral = null) }
        if (errorRut != null || errorCorreo != null) return


        if (estado.enviando) return   // evita doble toque (ponlo al inicio de onCrear)

        val claveTemporal = Seguridad.claveTemporal()
        val id = db.insertarAsegurado(rutLimpio, estado.correo, Seguridad.huella(claveTemporal))
        if (id == -1L) {
            _uiState.update { it.copy(errorGeneral = "No se pudo crear la cuenta. Revisa los datos") }
            return
        }

        _uiState.update { it.copy(enviando = true) }
        viewModelScope.launch {
            val enviado = EnvioCorreo.enviarClaveTemporal(estado.correo, claveTemporal, MotivoClave.CREACION)
            if (!enviado) {
                db.eliminarUsuario(id)
                _uiState.update {
                    it.copy(
                        enviando = false,
                        errorGeneral = "No pudimos enviar el correo al cliente. La cuenta no fue creada; inténtalo otra vez"
                    )
                }
            } else {
                _uiState.update { it.copy(enviando = false, rut = formatearRut(rutLimpio), cuentaCreada = true) }
            }
        }
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