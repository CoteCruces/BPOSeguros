package com.example.bposeguros.model

import androidx.annotation.StringRes
import com.example.bposeguros.R

enum class Etapa(val id: Long, @StringRes val etiqueta: Int) {
    RECIBIDO(1, R.string.etapa_recibido),
    EN_EVALUACION(2, R.string.etapa_en_evaluacion),
    EN_LIQUIDACION(3, R.string.etapa_en_liquidacion),
    CERRADO(4, R.string.etapa_cerrado);

    // RN-01: solo se avanza un paso (nunca se retrocede ni se salta).
    // El liquidador solo avanza de "En evaluación" a "En liquidación";
    // de "Recibido" a "En evaluación" se pasa al asignar un liquidador (RN-07).
    fun siguiente(): Etapa? = when (this) {
        EN_EVALUACION -> EN_LIQUIDACION
        else -> null
    }

    // Tabla de etapas del caso: con qué resultado se puede cerrar desde cada etapa
    fun resultadosPermitidos(): List<Resultado> = when (this) {
        RECIBIDO -> listOf(Resultado.DESISTIDO)
        EN_EVALUACION -> listOf(Resultado.RECHAZADO, Resultado.DESISTIDO)
        EN_LIQUIDACION -> listOf(Resultado.APROBADO, Resultado.RECHAZADO)
        CERRADO -> emptyList()
    }

    companion object {
        fun desdeId(id: Long): Etapa = entries.first { it.id == id }
    }
}
