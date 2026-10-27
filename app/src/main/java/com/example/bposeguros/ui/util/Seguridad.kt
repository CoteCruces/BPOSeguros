package com.example.bposeguros.ui.util

import java.security.MessageDigest
import kotlin.random.Random

// =====================================================================
//  Seguridad.kt  —  Contraseñas
//  La contraseña NUNCA se guarda tal cual en la BD: se guarda su "huella" (hash SHA-256).
//   - Es una transformación de un solo sentido: de "Andes2026!" sale siempre
//     el mismo texto largo, pero desde ese texto no se puede volver a la contraseña.
//   - Para revisar el ingreso se calcula la huella de lo escrito y se compara
//     con la guardada. Si alguien abre la BD, no ve las contraseñas.
// =====================================================================
object Seguridad {

    fun huella(clave: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())
        // Cada byte se escribe como 2 caracteres hexadecimales ("%02x") -> 64 caracteres en total
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Contraseña temporal fácil de escribir que cumple las reglas: "Temp" + 4 números + "!"  (ej: Temp4821!)
    fun claveTemporal(): String = "Temp" + Random.nextInt(1000, 10000) + "!"
}
