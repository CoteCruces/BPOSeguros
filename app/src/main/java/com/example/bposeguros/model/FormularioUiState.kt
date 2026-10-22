package com.example.bposeguros.model

// aquí va lo que la pantalla de inicio necesita
// los valores de los campos, las opciones marcadas y sus errores (null = sin error)
//Es una data class INMUTABLE (val): para cambiarla se crea una copia
//  con .copy(...). Así Compose detecta el cambio y vuelve a dibujar.

data class FormularioUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",
    val comuna: String = "",
    val comunas: List<String> = listOf(
        "Maipú", "Puente Alto", "Quilicura",
        "Concepción", "Rancagua", "Viña del Mar"
    ),
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val aceptaTerminos: Boolean = false,

    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorComuna: String? = null,
    val errorContrasena: String? = null,
    val errorTerminos: String? = null,

    val registroExitoso: Boolean = false
)