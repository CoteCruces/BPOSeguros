package com.example.bposeguros.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.ui.styles.EstilosCampo
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas
import java.util.Calendar
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    onFechaElegida: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    diasAtras: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(etiqueta) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            },
            isError = error != null,
            supportingText = {
                when {
                    error != null -> Text(error)
                    ayuda != null -> Text(ayuda)
                }
            },
            singleLine = true,
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    onClickLabel = stringResource(R.string.btn_elegir_fecha),
                    role = Role.Button
                ) { mostrarCalendario = true }
        )
    }

    if (mostrarCalendario) {
        val hoy = Fechas.hoy()
        val desde = Fechas.haceDias(diasAtras)
        val limiteSuperior = Fechas.aMilisegundos(hoy) ?: System.currentTimeMillis()
        val limiteInferior = Fechas.aMilisegundos(desde) ?: 0L
        val meses = remember(hoy, desde) { Fechas.mesesEntre(desde, hoy).reversed() }

        val fechaInicial = Fechas.desdeTextoUsuario(valor)
            ?.let { Fechas.aMilisegundos(it) }
            ?.takeIf { it in limiteInferior..limiteSuperior }

        var elegida by remember { mutableStateOf(fechaInicial) }
        var mesMostrado by remember { mutableStateOf(Fechas.inicioDeMes(fechaInicial ?: limiteSuperior)) }

        AlertDialog(
            onDismissRequest = { mostrarCalendario = false },
            title = { Text(etiqueta) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    CampoDesplegable(
                        valor = Fechas.nombreMes(mesMostrado),
                        etiqueta = stringResource(R.string.campo_mes),
                        opciones = meses.map { Fechas.nombreMes(it) },
                        onElegir = { indice -> mesMostrado = meses[indice] }
                    )
                    CuadriculaMes(
                        mes = mesMostrado,
                        elegida = elegida,
                        minimo = limiteInferior,
                        maximo = limiteSuperior,
                        onElegir = { elegida = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        elegida?.let { onFechaElegida(Fechas.paraCampo(Fechas.desdeMilisegundos(it))) }
                        mostrarCalendario = false
                    },
                    enabled = elegida != null
                ) { Text(stringResource(R.string.btn_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}

/** Los días de un mes en filas de lunes a domingo. Los días fuera de [minimo]..[maximo] no se pueden tocar. */
@Composable
private fun CuadriculaMes(
    mes: Long,
    elegida: Long?,
    minimo: Long,
    maximo: Long,
    onElegir: (Long) -> Unit
) {
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = mes }
    val desfase = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7 // lunes = 0 ... domingo = 6
    val diasDelMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val semanas = (desfase + diasDelMes + 6) / 7
    val unDia = 24L * 60 * 60 * 1000

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { letra ->
                Text(
                    text = letra,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        for (semana in 0 until semanas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (columna in 0 until 7) {
                    val dia = semana * 7 + columna - desfase + 1
                    if (dia < 1 || dia > diasDelMes) {
                        Spacer(modifier = Modifier.weight(1f).height(40.dp))
                    } else {
                        val millis = mes + (dia - 1) * unDia
                        val habilitado = millis in minimo..maximo
                        val seleccionado = millis == elegida
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable(enabled = habilitado, role = Role.Button) { onElegir(millis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dia",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    seleccionado -> MaterialTheme.colorScheme.onPrimary
                                    habilitado -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
