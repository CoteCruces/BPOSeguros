package com.example.bposeguros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.bposeguros.R
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas


@Composable
fun DialogoBuscarPorFecha(
    onBuscar: (desde: String, hasta: String) -> Unit,
    onCancelar: () -> Unit
) {
    val hoy = Fechas.hoy()
    val anioActual = hoy.take(4).toInt()
    val mesActual = hoy.substring(5, 7).toInt()
    val anios = remember(anioActual) { listOf(anioActual, anioActual - 1, anioActual - 2) }

    var anio by remember { mutableStateOf(anioActual) }
    var mes by remember { mutableStateOf<Int?>(null) }

    val textoTodos = stringResource(R.string.opcion_todos_los_meses)
    val mesMaximo = if (anio == anioActual) mesActual else 12
    val opcionesMes = listOf(textoTodos) + (1..mesMaximo).map { Fechas.nombreDelMes(it) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.titulo_buscar_fecha)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                CampoDesplegable(
                    valor = anio.toString(),
                    etiqueta = stringResource(R.string.campo_anio),
                    opciones = anios.map { it.toString() },
                    onElegir = { indice ->
                        anio = anios[indice]
                        val maximo = if (anio == anioActual) mesActual else 12
                        if ((mes ?: 0) > maximo) mes = null
                    }
                )
                CampoDesplegable(
                    valor = mes?.let { Fechas.nombreDelMes(it) } ?: textoTodos,
                    etiqueta = stringResource(R.string.campo_mes),
                    opciones = opcionesMes,
                    onElegir = { indice -> mes = if (indice == 0) null else indice }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val (desde, hasta) = Fechas.rangoDelPeriodo(anio, mes)
                onBuscar(desde, hasta)
            }) { Text(stringResource(R.string.btn_buscar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.btn_cancelar)) }
        }
    )
}