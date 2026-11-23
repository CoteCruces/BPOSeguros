package com.example.bposeguros.data

import android.content.Context
import android.widget.Toast

fun Context.avisoCorto(texto: String) {
    if (texto.isNotBlank()) Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
}