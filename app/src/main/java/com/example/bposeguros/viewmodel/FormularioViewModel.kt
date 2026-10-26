package com.example.bposeguros.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()



    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    /*fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null, registroExitoso = false) }
    }*/

    /*fun onComunaSeleccionada(comuna: String) {
        _uiState.update { it.copy(comuna = comuna, errorComuna = null) }
    }*/

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }



    fun onIngresar(): Usuario? {
        val estado = _uiState.value


        val palabrasNombre = estado.nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }


        val errorNombre = when {
            estado.nombre.isBlank() -> "El nombre es obligatorio"
            !estado.nombre.all { it.isLetter() || it == ' ' } ->
                "El nombre no puede tener números ni símbolos"
            palabrasNombre.size < 3 ->
                "Escribe tu nombre y apellido (al menos 2 palabras)"
            palabrasNombre.any { it.length < 3 } ->
                "Cada palabra debe tener al menos 3 letras"
            estado.nombre.count { it.isLetter() } < 7 ->
                "El nombre y apellido deben sumar al menos 7 caracteres"
            else -> null
        }



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



        /*val digitosTelefono = estado.telefono.filter { it.isDigit() } */

        /*val telefonoValido = (digitosTelefono.length == 11 && digitosTelefono.startsWith("569")) ||
                (digitosTelefono.length == 9 && digitosTelefono.startsWith("9"))*/

        /*val errorTelefono = when {
            estado.telefono.isBlank() -> null // Opcional: no hay error si está vacío
            !telefonoValido -> "Ingresa un celular válido (ejemplo: +56 9 12345678)"
            else -> null
        }*/

        /*val errorComuna = if (estado.comuna.isBlank()) "Elige tu comuna" else null*/


        val errorContrasena = when {
            estado.contrasena.length < 8 -> "Debe tener al menos 8 caracteres"
            !estado.contrasena.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula" //any { ... } Recorre la cadena y devuelve true si encuentra al menos un caracter que cumpla la condición
            !estado.contrasena.any { it.isDigit() } -> "Debe incluir al menos un número"
            estado.contrasena.all { it.isLetterOrDigit() } ->
                "Debe incluir al menos un carácter especial (por ejemplo @, # o !)"
            else -> null
        }





        val hayErrores = listOf(errorNombre, errorRut, errorCorreo, errorContrasena)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorRut = errorRut,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                registroExitoso = !hayErrores,
                contrasena = if (hayErrores) it.contrasena else ""
            )   //errorTelefono = errorTelefono,
                //errorComuna = errorComuna,
        }


        return if (hayErrores) null else Usuario(
            nombre = estado.nombre.trim(),
            rut = estado.rut.replace(".", "").replace("-", "").trim().uppercase(), //.replace(".", "").replace("-", "") Remueve todos los puntos y guiones.
            correo = estado.correo
        )
    }


    fun onLimpiar() {
        _uiState.value = FormularioUiState()
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