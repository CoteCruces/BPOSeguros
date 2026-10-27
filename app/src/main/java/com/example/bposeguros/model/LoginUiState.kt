package com.example.bposeguros.model

// Estado de la pantalla de bienvenida: el interruptor, el RUT, la contraseña y sus errores.
// esLiquidador = interruptor "Soy liquidador de Andes" (el que tenía el proyecto original).
// Clientes y liquidadores escriben lo mismo (RUT + contraseña); el interruptor sirve de FILTRO:
// la cuenta encontrada en la BD tiene que ser del tipo elegido.
// null = sin error. Es inmutable: para cambiarlo se crea una copia con .copy(...)
data class LoginUiState(
    val esLiquidador: Boolean = false,
    val rut: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val errorRut: String? = null,
    val errorClave: String? = null
)
