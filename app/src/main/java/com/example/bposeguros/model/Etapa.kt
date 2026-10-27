package com.example.bposeguros.model

import androidx.annotation.StringRes
import com.example.bposeguros.R

enum class Etapa(val id: Long, @StringRes val etiqueta: Int) {
    RECIBIDO(1, R.string.etapa_recibido),
    EN_EVALUACION(2, R.string.etapa_en_evaluacion),
    EN_LIQUIDACION(3, R.string.etapa_en_liquidacion),
    CERRADO(4, R.string.etapa_cerrado);

    fun siguiente(): Etapa? = when (this) {
        EN_EVALUACION -> EN_LIQUIDACION
        else -> null
    }

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
