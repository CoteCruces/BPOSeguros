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

// =====================================================================
//  LoginViewModel.kt  —  PANTALLA DE BIENVENIDA (ingreso con RUT + contraseña)
//  - Clientes y liquidadores entran IGUAL: con su RUT y su contraseña.
//  - El interruptor "Soy liquidador de Andes" es un FILTRO: la fila encontrada en la BD
//    trae el id_rol y debe coincidir con lo elegido. Luego AppNavegacion decide a qué pantalla ir.
//  - No navega: onIngresar() devuelve el Usuario (o null si algo falló).
// =====================================================================
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ----- Eventos -----

    // Interruptor cliente / liquidador. Se borran los errores porque cambió lo que se está pidiendo.
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

    /** Botón "Ingresar": valida el formato, busca RUT + clave en la BD y entrega el Usuario (o null). */
    fun onIngresar(): Usuario? {
        val estado = _uiState.value

        // 1) Formato
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

        // 2) ¿Existe alguien con ese RUT y esa contraseña?
        // Se compara la HUELLA de lo escrito con la huella guardada (la clave real nunca se guarda).
        val usuario = db.buscarPorRutYClave(limpiarRut(estado.rut), Seguridad.huella(estado.clave))

        // Por seguridad NO se dice cuál de los dos está mal: así nadie puede averiguar
        // si un RUT tiene cuenta probando contraseñas al azar.
        val error = when {
            usuario == null -> "RUT o contraseña incorrectos"
            !usuario.activo -> "Tu cuenta no está activa. Comunícate con Andes"
            usuario.rolId == Rol.ID_ANALISTA -> "Por ahora solo asegurados y liquidadores usan la app"
            // Filtro del interruptor (solo se llega aquí si el RUT y la contraseña ya estaban bien)
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
        // Se borra la contraseña del estado: no queda escrita al volver a esta pantalla
        // (el interruptor se mantiene como estaba)
        _uiState.value = LoginUiState(esLiquidador = estado.esLiquidador)
        return usuario
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
