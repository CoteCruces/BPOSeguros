package com.example.bposeguros.ui.util

object Validaciones {

    private val FORMATO_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun limpiarRut(valor: String): String =
        valor.replace(".", "").replace("-", "").trim().uppercase()

    fun formatearRut(rut: String): String {
        if (rut.length < 2) return rut
        val cuerpo = rut.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${rut.last()}"
    }

    fun rutValido(valor: String): Boolean {
        val limpio = limpiarRut(valor)
        if (limpio.length < 8 || limpio.length > 9) return false
        val cuerpo = limpio.dropLast(1)
        val digitoVerificador = limpio.last()
        if (!cuerpo.all { it in '0'..'9' }) return false
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

    fun errorCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !correo.matches(FORMATO_CORREO) -> "Ingresa un correo válido (ejemplo: usuario@dominio.com)"
        else -> null
    }

    fun errorNombreCompleto(nombre: String): String? {
        val palabras = nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            nombre.isBlank() -> "El nombre es obligatorio"
            !nombre.all { it.isLetter() || it == ' ' } -> "El nombre no puede tener números ni símbolos"
            palabras.size < 2 -> "Escribe nombre y apellido"
            palabras.any { it.length < 2 } -> "Cada palabra debe tener al menos 2 letras"
            nombre.trim().length > 100 -> "El nombre es demasiado largo (máximo 100 caracteres)"
            else -> null
        }
    }
}