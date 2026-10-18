package com.example.bposeguros.model

data class Usuario (
    val id: Long = 0,
    val nombre: String,
    val rut: String = "",
    val correo: String = "",
    val comuna: String = "",
    val codigo: String = "",
    val equipo: String = "",
    val activo: Boolean = true,
    val rolId: Long = Rol.ID_ASEGURADO,
    val rolNombre: String = ""
) {

    //trim(): Elimina espacios en blanco accidentales al inicio o al final
    //substringBefore(" "): Devuelve la cadena de texto desde el inicio hasta el primer espacio ej: Juan
    fun primerNombre(): String = nombre.trim().substringBefore(" ")


    // RNF-10: el RUT se muestra parcialmente oculto  -> ejemplo Para el RUT "158423715"
    //return "**.***." -> mantiene oculto los primeros digitos por privacidad
    //rut.dropLast(1): Elimina el dígito verificador para trabajar con los números anteriores
    //.takeLast(3): Extrae los últimos 3 dígitos de la cadena resultante ej: "371"
    //"-" + rut.last(): Añade un guion seguido del último carácter del RUT (el dígito verificador) ej: "**.***.371-5"
    fun rutOculto(): String {
        if (rut.length < 4) return ""
        return "**.***." + rut.dropLast(1).takeLast(3) + "-" + rut.last()
    }
}