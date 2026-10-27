package com.example.bposeguros.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas {

    private fun formato(patron: String) = SimpleDateFormat(patron, Locale.US).apply {
        isLenient = false
    }

    fun ahora(): String = formato("yyyy-MM-dd HH:mm").format(Date())

    fun hoy(): String = formato("yyyy-MM-dd").format(Date())

    fun desdeTextoUsuario(texto: String): String? = try {
        val fecha = formato("dd-MM-yyyy").parse(texto.trim())
        if (fecha == null) null else formato("yyyy-MM-dd").format(fecha)
    } catch (e: Exception) {
        null
    }

    fun paraMostrar(textoBd: String?): String {
        if (textoBd.isNullOrBlank()) return "—"
        val fecha = textoBd.take(10)
        val hora = textoBd.drop(10).trim()
        val partes = fecha.split("-")
        if (partes.size != 3) return textoBd
        val bonita = "${partes[2]}-${partes[1]}-${partes[0]}"
        return if (hora.isEmpty()) bonita else "$bonita $hora"
    }

    fun diasEntre(desde: String, hasta: String): Long {
        val f = formato("yyyy-MM-dd")
        val inicio = f.parse(desde.take(10)) ?: return 0
        val fin = f.parse(hasta.take(10)) ?: return 0
        return Math.round((fin.time - inicio.time) / (1000.0 * 60 * 60 * 24))
    }

    fun desdeMilisegundos(milisegundos: Long): String =
        formato("yyyy-MM-dd").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(milisegundos))

    fun haceAnios(anios: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.YEAR, -anios)
        return formato("yyyy-MM-dd").format(calendario.time)
    }

    fun paraCampo(textoBd: String): String = if (textoBd.isBlank()) "" else paraMostrar(textoBd)
}
