package com.example.bposeguros.model

data class Usuario (
    val id: Long = 0,
    val nombre: String,
    val rut: String = "",
    val correo: String = "",
    val comuna: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val foto: String = "",
    val claveTemporal: Boolean = false,
    val codigo: String = "",
    val equipo: String = "",
    val activo: Boolean = true,
    val rolId: Long = Rol.ID_ASEGURADO,
    val rolNombre: String = ""
) {

    val esAsegurado: Boolean get() = rolId == Rol.ID_ASEGURADO

    
    fun primerNombre(): String = nombre.trim().substringBefore(" ")



    fun rutOculto(): String {
        if (rut.length < 4) return ""
        return "**.***." + rut.dropLast(1).takeLast(3) + "-" + rut.last()
    }

    fun comoResponsable(): String {
        val partes = nombre.trim().split(" ")
        val corto = if (partes.size >= 2) "${partes[0].first()}. ${partes[1]}" else nombre
        return if (codigo.isBlank()) corto else "$corto ($codigo)"
    }
}