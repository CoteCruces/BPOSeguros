package com.example.bposeguros.ui.util

import java.security.MessageDigest
import kotlin.random.Random

object Seguridad {

    fun huella(clave: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun claveTemporal(): String = "Temp" + Random.nextInt(1000, 10000) + "!"
}
