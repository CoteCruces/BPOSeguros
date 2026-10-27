package com.example.bposeguros.model

// La contraseña NO viaja en este modelo: se queda en la BD (como huella) y solo la revisa SiniestroDbHelper.
data class Usuario (
    val id: Long = 0,
    val nombre: String = "",           // un cliente nuevo parte SIN nombre: lo completa en "Mi perfil"
    val rut: String = "",
    val correo: String = "",
    val comuna: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",  // formato BD "aaaa-mm-dd" ("" = sin completar)
    val foto: String = "",             // ruta del archivo de la foto ("" = avatar por defecto)
    val claveTemporal: Boolean = false, // true = debe cambiar la contraseña al entrar
    val codigo: String = "",
    val equipo: String = "",
    val activo: Boolean = true,
    val rolId: Long = Rol.ID_ASEGURADO,
    val rolNombre: String = ""
) {
    val esAsegurado: Boolean get() = rolId == Rol.ID_ASEGURADO

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

    // El RUT completo con puntos y guion, para "Mi perfil": "158423715" -> "15.842.371-5"
    // chunked(3) corta de a 3 dígitos; se da vuelta antes y después para cortar desde la derecha.
    fun rutFormateado(): String {
        if (rut.length < 2) return rut
        val cuerpo = rut.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${rut.last()}"
    }

    // Cómo aparece el colaborador como responsable en el historial: "P. Fernández (LIQ-01)"
    fun comoResponsable(): String {
        val partes = nombre.trim().split(" ")
        val corto = if (partes.size >= 2) "${partes[0].first()}. ${partes[1]}" else nombre
        return if (codigo.isBlank()) corto else "$corto ($codigo)"
    }
}
