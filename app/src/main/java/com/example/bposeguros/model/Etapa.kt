package com.example.bposeguros.model

import androidx.annotation.StringRes
import com.example.bposeguros.R

enum class Etapa(val id: Long, @StringRes val etiqueta: Int) {
    RECIBIDO(1, R.string.etapa_recibido),
    EN_EVALUACION(2, R.string.etapa_en_evaluacion),
    EN_LIQUIDACION(3, R.string.etapa_en_liquidacion),
    CERRADO(4, R.string.etapa_cerrado);

    // RN-01: solo se avanza (nunca se retrocede). El cierre anticipado se permite desde cualquier etapa abierta.
    fun siguientes(): List<Etapa> = when (this) {
        RECIBIDO -> listOf(EN_EVALUACION, CERRADO)
        EN_EVALUACION -> listOf(EN_LIQUIDACION, CERRADO)
        EN_LIQUIDACION -> listOf(CERRADO)
        CERRADO -> emptyList()
    }

    companion object {
        fun desdeId(id: Long): Etapa = entries.first { it.id == id }
    }
}