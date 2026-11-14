package com.example.bposeguros.ui.util

import java.security.MessageDigest
import java.security.SecureRandom

object Seguridad {

    private val generador = SecureRandom()

    private const val MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val MINUSCULAS = "abcdefghijkmnpqrstuvwxyz"
    private const val NUMEROS = "23456789"
    private const val ESPECIALES = "@#!$%&*?"
    private const val LARGO_CLAVE = 12

    fun huella(clave: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun claveTemporal(): String {
        val todos = MAYUSCULAS + MINUSCULAS + NUMEROS + ESPECIALES
        val caracteres = mutableListOf(
            elegir(MAYUSCULAS),
            elegir(MINUSCULAS),
            elegir(NUMEROS),
            elegir(ESPECIALES)
        )
        while (caracteres.size < LARGO_CLAVE) caracteres.add(elegir(todos))

        for (i in caracteres.lastIndex downTo 1) {
            val j = generador.nextInt(i + 1)
            val auxiliar = caracteres[i]
            caracteres[i] = caracteres[j]
            caracteres[j] = auxiliar
        }
        return caracteres.joinToString("")
    }

    private fun elegir(conjunto: String): Char = conjunto[generador.nextInt(conjunto.length)]
}