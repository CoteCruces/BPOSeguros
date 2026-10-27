package com.example.bposeguros.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.EtapaCerrado
import com.example.bposeguros.ui.theme.EtapaEvaluacion
import com.example.bposeguros.ui.theme.EtapaLiquidacion
import com.example.bposeguros.ui.theme.EtapaRecibido
import com.example.bposeguros.ui.theme.TextoOscuro

// =====================================================================
//  EtapaVisual.kt  —  Cómo se ve la etapa del siniestro
//  La etapa es lo PRIMERO que debe entenderse y no puede depender solo del
//  color (RNF-04, baja visión): cada etapa tiene color + ÍCONO + TEXTO.
// =====================================================================

// Ícono y color de cada etapa. "when" sobre un enum: Kotlin obliga a cubrir las 4.
fun iconoDe(etapa: Etapa): Int = when (etapa) {
    Etapa.RECIBIDO -> R.drawable.ic_documento
    Etapa.EN_EVALUACION -> R.drawable.ic_reloj
    Etapa.EN_LIQUIDACION -> R.drawable.ic_compania
    Etapa.CERRADO -> R.drawable.ic_check
}

fun colorDe(etapa: Etapa): Color = when (etapa) {
    Etapa.RECIBIDO -> EtapaRecibido
    Etapa.EN_EVALUACION -> EtapaEvaluacion
    Etapa.EN_LIQUIDACION -> EtapaLiquidacion
    Etapa.CERRADO -> EtapaCerrado
}

// Etiqueta chica (tipo "chip") para las listas: [ícono  En evaluación]
@Composable
fun EtiquetaEtapa(etapa: Etapa, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(colorDe(etapa), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(iconoDe(etapa)), contentDescription = null, tint = TextoOscuro, modifier = Modifier.size(18.dp))
        Text(
            text = stringResource(etapa.etiqueta),
            style = MaterialTheme.typography.labelLarge,
            color = TextoOscuro,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

// =====================================================================
//  ANIMACIÓN: barra de progreso de las 4 etapas.
//  animateFloatAsState = cuando cambia el valor "objetivo", Compose dibuja los
//  valores intermedios durante 900 ms -> la barra "se llena" sola.
//  Para que también se anime al ABRIR la pantalla, el objetivo parte en 0
//  y un LaunchedEffect lo cambia al valor real apenas se dibuja.
// =====================================================================
@Composable
fun IndicadorEtapas(etapaActual: Etapa, modifier: Modifier = Modifier) {
    val etapas = Etapa.entries                                   // las 4 en orden
    val posicion = etapas.indexOf(etapaActual)                   // 0..3

    var objetivo by remember { mutableStateOf(0f) }
    LaunchedEffect(etapaActual) { objetivo = (posicion + 1) / etapas.size.toFloat() }
    val progreso by animateFloatAsState(targetValue = objetivo, animationSpec = tween(900), label = "progreso")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = colorDe(etapaActual),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            etapas.forEachIndexed { i, etapa ->
                PasoEtapa(etapa = etapa, alcanzada = i <= posicion, actual = i == posicion, modifier = Modifier.weight(1f))
            }
        }
    }
}

// Un paso: círculo con ícono + nombre. El color del círculo también se anima.
@Composable
private fun PasoEtapa(etapa: Etapa, alcanzada: Boolean, actual: Boolean, modifier: Modifier = Modifier) {
    val fondo by animateColorAsState(
        targetValue = if (alcanzada) colorDe(etapa) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(900),
        label = "colorPaso"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (actual) 40.dp else 32.dp)
                .background(fondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(iconoDe(etapa)),
                contentDescription = null,
                tint = if (alcanzada) TextoOscuro else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = stringResource(etapa.etiqueta),
            style = if (actual) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.espacioMini)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EtapaVisualPreview() {
    BPOSegurosTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EtiquetaEtapa(Etapa.EN_EVALUACION)
            IndicadorEtapas(Etapa.EN_EVALUACION)
        }
    }
}
