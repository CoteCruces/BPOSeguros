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

    fun aMilisegundos(textoBd: String): Long? = try {
        formato("yyyy-MM-dd").apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(textoBd.take(10))?.time
    } catch (e: Exception){
        null
    }

    fun haceDias(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -dias)
        return formato("yyyy-MM-dd").format(calendario.time)
    }

    fun mesesEntre(desde: String, hasta: String): List<Long> {
        val inicio = aMilisegundos(desde.take(7) + "-01") ?: return emptyList()
        val fin = aMilisegundos(hasta.take(7) + "-01") ?: return emptyList()
        val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = inicio }
        val meses = mutableListOf<Long>()
        while (calendario.timeInMillis <= fin) {
            meses.add(calendario.timeInMillis)
            calendario.add(Calendar.MONTH, 1)
        }
        return meses
    }

    fun nombreMes(milisegundosUtc: Long): String {
        val formatoMes = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-CL"))
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
        return formatoMes.format(Date(milisegundosUtc)).replaceFirstChar { it.uppercase() }
    }


    fun inicioDeMes(milisegundosUtc: Long): Long =
        aMilisegundos(desdeMilisegundos(milisegundosUtc).take(7) + "-01") ?: milisegundosUtc

    fun nombreDelMes(mes: Int): String {
        val formatoMes = SimpleDateFormat("MMMM", Locale.forLanguageTag("es-CL"))
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
        val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2000, mes - 1, 1)
        }
        return formatoMes.format(calendario.time).replaceFirstChar { it.uppercase() }
    }


    fun rangoDelPeriodo(anio: Int, mes: Int?): Pair<String, String> {
        if (mes == null) {
            return String.format(Locale.US, "%04d-01-01", anio) to String.format(Locale.US, "%04d-12-31", anio)
        }
        val calendario = Calendar.getInstance().apply {
            clear()
            set(anio, mes - 1, 1)
        }
        val ultimoDia = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-01", anio, mes) to
                String.format(Locale.US, "%04d-%02d-%02d", anio, mes, ultimoDia)
    }


    fun haceAnios(anios: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.YEAR, -anios)
        return formato("yyyy-MM-dd").format(calendario.time)
    }

    fun paraCampo(textoBd: String): String = if (textoBd.isBlank()) "" else paraMostrar(textoBd)
}
