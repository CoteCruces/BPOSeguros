package com.example.bposeguros.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================
//  Fechas.kt  —  Ayudantes para fechas
//  En la BD las fechas se guardan como TEXTO con formato "aaaa-mm-dd"
//  (así se pueden ordenar con ORDER BY). Al usuario se le muestran "dd-mm-aaaa".
//  Se usa SimpleDateFormat porque funciona desde Android 7 (minSdk 24);
//  LocalDate (java.time) necesita Android 8 o más.
// =====================================================================
object Fechas {

    private fun formato(patron: String) = SimpleDateFormat(patron, Locale.US).apply {
        isLenient = false      // NO acepta fechas inventadas como 31-02-2026
    }

    // Fecha y hora actual para guardar en la BD: "2026-10-21 15:30"
    fun ahora(): String = formato("yyyy-MM-dd HH:mm").format(Date())

    // Solo la fecha de hoy: "2026-10-21"
    fun hoy(): String = formato("yyyy-MM-dd").format(Date())

    // Lo que escribe el usuario ("02-09-2026") -> formato de BD ("2026-09-02").
    // Devuelve null si la fecha no existe o está mal escrita.
    fun desdeTextoUsuario(texto: String): String? = try {
        val fecha = formato("dd-MM-yyyy").parse(texto.trim())
        if (fecha == null) null else formato("yyyy-MM-dd").format(fecha)
    } catch (e: Exception) {
        null
    }

    // Formato de BD -> formato para mostrar. "2026-09-02 18:40" -> "02-09-2026 18:40"
    fun paraMostrar(textoBd: String?): String {
        if (textoBd.isNullOrBlank()) return "—"
        val fecha = textoBd.take(10)                        // "2026-09-02"
        val hora = textoBd.drop(10).trim()                  // "18:40" o ""
        val partes = fecha.split("-")
        if (partes.size != 3) return textoBd
        val bonita = "${partes[2]}-${partes[1]}-${partes[0]}"
        return if (hora.isEmpty()) bonita else "$bonita $hora"
    }

    // Días corridos entre dos fechas "aaaa-mm-dd" (para RN-05 y para "días en la etapa")
    fun diasEntre(desde: String, hasta: String): Long {
        val f = formato("yyyy-MM-dd")
        val inicio = f.parse(desde.take(10)) ?: return 0
        val fin = f.parse(hasta.take(10)) ?: return 0
        // Math.round: evita perder un día cuando hay cambio de horario (un día de 23 horas)
        return Math.round((fin.time - inicio.time) / (1000.0 * 60 * 60 * 24))
    }
}
