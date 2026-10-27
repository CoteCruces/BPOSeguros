package com.example.bposeguros.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas
@Composable
fun TarjetaSiniestro(
    siniestro: Siniestro,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extra: String? = null,
    alerta: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = siniestro.numero,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaEtapa(siniestro.etapa)
            }
            FilaDato(R.drawable.ic_compania, siniestro.companiaNombre)
            FilaDato(R.drawable.ic_documento, siniestro.tipoNombre)
            if (extra != null) FilaDato(R.drawable.ic_persona, extra)
            FilaDato(
                R.drawable.ic_reloj,
                "Última actualización: " + Fechas.paraMostrar(siniestro.ultimaActualizacion ?: siniestro.fechaCambioEtapa)
            )
            if (alerta != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_advertencia), contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
                    )
                    Text(
                        alerta, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilaDato(@DrawableRes icono: Int, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(icono), contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)
        )
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = Dimens.espacioChico))
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSiniestroPreview() {
    BPOSegurosTheme {
        TarjetaSiniestro(
            siniestro = Siniestro(
                numero = "SIN-26-004801", aseguradoId = 1, companiaId = 1, companiaNombre = "Seguros Cordillera",
                tipoId = 1, tipoNombre = "Vehículo – choque o colisión", fechaOcurrencia = "2026-09-02",
                fechaReporte = "2026-09-02", comuna = "Maipú", etapa = Etapa.EN_LIQUIDACION,
                fechaCambioEtapa = "2026-09-18", ultimaActualizacion = "2026-09-18 15:45"
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
