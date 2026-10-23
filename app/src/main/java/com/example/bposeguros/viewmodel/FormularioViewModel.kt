package com.example.bposeguros.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//  RegistroViewModel.kt  —  LÓGICA DE LA PANTALLA DE INGRESO
//  - Guarda el estado y sobrevive a la rotación (lo escrito no se pierde).
//  - Expone el estado en SOLO LECTURA (StateFlow).
//  - Recibe los eventos de la vista con funciones "on...".
//  - VALIDA el formulario (la vista NO decide si un RUT es válido).
//  - No navega: onIngresar() devuelve el Usuario (o null si hay errores)
//    y AppNavegacion decide a dónde ir.
//  Regla: el ViewModel NUNCA importa nada de Compose (ni Text ni Color).

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())    // privado y mutable
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()    // público, solo lectura

    // ----- Eventos de los campos: cada letra escrita llega aquí -----
    // Al escribir, se borra el error de ese campo y el mensaje de éxito.

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null, registroExitoso = false) }
    }

    fun onComunaSeleccionada(comuna: String) {
        _uiState.update { it.copy(comuna = comuna, errorComuna = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onAceptaTerminos(valor: Boolean){
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    // ----- Botones -----

    /** Botón "Ingresar": valida todo y, si está bien, entrega el Usuario. */

    fun onIngresar(): Usuario? {
        val estado = _uiState.value

        // Nombre: solo letras y espacios, al menos 2 palabras (nombre + apellido) y mínimo 6 letras
        val palabrasNombre = estado.nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        // trim(): quita espacios al inicio y al final
        // split(Regex("\\s+")): separa el texto en palabras, aunque haya varios espacios seguidos
        // filter { it.isNotBlank() }: descarta trozos vacíos

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


        // RUT con dígito verificador (RN-03)
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


        // Teléfono: opcional; si se escribe, debe ser celular chileno válido (9 u 11 dígitos)
        val digitosTelefono = estado.telefono.filter { it.isDigit() }   //.filter { ... }: Recorre el texto caracter por caracter y conserva únicamente los elementos que cumplan la condición {
                                                                        //it.isDigit()} que evalúa si el carácter actual es un número del 0 al 9
        val telefonoValido = (digitosTelefono.length == 11 && digitosTelefono.startsWith("569")) ||  //.startsWith("569")   Verifica que la cadena comience con los tres dígitos "569" ó
                (digitosTelefono.length == 9 && digitosTelefono.startsWith("9"))                     //.startsWith("9")  que comience con en prefijo 9

        val errorTelefono = when {
            estado.telefono.isBlank() -> null // Opcional: no hay error si está vacío
            !telefonoValido -> "Ingresa un celular válido (ejemplo: +56 9 12345678)"
            else -> null
        }

        val errorComuna = if (estado.comuna.isBlank()) "Elige tu comuna" else null

        // Contraseña: mayúscula, número y carácter especial
        val errorContrasena = when {
            estado.contrasena.length < 8 -> "Debe tener al menos 8 caracteres"
            !estado.contrasena.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula" //any { ... } Recorre la cadena y devuelve true si encuentra al menos un caracter que cumpla la condición
            !estado.contrasena.any { it.isDigit() } -> "Debe incluir al menos un número"
            estado.contrasena.all { it.isLetterOrDigit() } ->
                "Debe incluir al menos un carácter especial (por ejemplo @, # o !)"
            else -> null
        }
        /*
        Contraseña que evalúa todas las reglas simultáneamente
        val contrasena = estado.contrasena
        val faltantes = mutableListOf<String>()
        if (contrasena.isBlank()) {
            faltantes.add("es obligatoria")
        } else {
            if (contrasena.length < 8) faltantes.add("mínimo 8 caracteres")
            if (!contrasena.any { it.isUpperCase() }) faltantes.add("al menos 1 mayúscula")
            if (!contrasena.any { it.isDigit() }) faltantes.add("al menos 1 número")
            if (contrasena.all { it.isLetterOrDigit() }) faltantes.add("al menos 1 carácter especial (ej: @, #, !)")
        }
        val errorContrasena = if (faltantes.isNotEmpty()) {
            "La contraseña debe tener: " + faltantes.joinToString(", ")
        } else null
        */

        val errorTerminos = if (!estado.aceptaTerminos)
            "Debes aceptar los términos" else null


        val hayErrores = listOf(errorNombre, errorRut, errorCorreo, errorTelefono, errorComuna, errorContrasena, errorTerminos)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorRut = errorRut,
                errorCorreo = errorCorreo,
                errorTelefono = errorTelefono,
                errorComuna = errorComuna,
                errorContrasena = errorContrasena,
                errorTerminos = errorTerminos,
                registroExitoso = !hayErrores,
                // RNF-08: la clave solo se valida, nunca se guarda
                contrasena = if (hayErrores) it.contrasena else ""
            )
        }

        // Con errores -> null;  sin errores -> el Usuario (rut sin puntos ni guion)
        //Si hayErrores es true: La función se interrumpe y retorna null. Le comunica a la vista:
        //"Hay campos con errores en la pantalla, la validación falló y no creé ningún usuario".
        return if (hayErrores) null else Usuario(
            nombre = estado.nombre.trim(),  //trim() Elimina espacios en blanco ej " Carlos Mora " por "Carlos Mora"
            rut = estado.rut.replace(".", "").replace("-", "").trim().uppercase(), //.replace(".", "").replace("-", "") Remueve todos los puntos y guiones.
            // Si el usuario escribió "15.842.371-5", lo transforma en "158423715", elimina los espacios en blanco y transforma la k en K si así terminara el rut
            correo = estado.correo
        )
    }

    /** Botón "Limpiar": vuelve al estado inicial. */
    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }

    // Algoritmo módulo 11 del RUT chileno. Privada: solo la usa este ViewModel.
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