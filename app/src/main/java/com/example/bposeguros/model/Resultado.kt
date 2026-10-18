package com.example.bposeguros.model

import androidx.annotation.StringRes
import com.example.bposeguros.R

enum class Resultado (@StringRes val etiqueta: Int) {
    APROBADO(R.string.resultado_aprobado),
    RECHAZADO(R.string.resultado_rechazado),
    DESISTIDO(R.string.resultado_desistido)
}