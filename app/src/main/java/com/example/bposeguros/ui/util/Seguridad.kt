package com.example.bposeguros.util

import java.security.MessageDigest
import kotlin.random.Random


object Seguridad {

    fun huella(clave: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())
        // Cada byte se escribe como 2 caracteres hexadecimales ("%02x") -> 64 caracteres en total
        return bytes.joinToString("") { "%02x".format(it) }
    }


    fun claveTemporal(): String = "Temp" + Random.nextInt(1000, 10000) + "!"
}
