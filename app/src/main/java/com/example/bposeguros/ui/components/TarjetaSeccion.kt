package com.example.bposeguros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens

// =====================================================================
//  TarjetaSeccion.kt  —  CONTENEDOR CARD CON TÍTULO + "SLOT"
//  Card = contenedor con fondo, esquinas redondeadas y sombra.
//  Recibe su contenido como PARÁMETRO (slot):
//      contenido: @Composable ColumnScope.() -> Unit
//  Quien lo usa decide qué va adentro:
//      TarjetaSeccion(titulo = "Datos") { CampoTexto(...) }
// =====================================================================
@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSeccionPreview() {
    BPOSegurosTheme {
        TarjetaSeccion(titulo = "Datos del siniestro", modifier = Modifier.padding(16.dp)) {
            Text("Cualquier contenido puede ir aquí dentro.")
        }
    }
}