package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.bposeguros.R
import com.example.bposeguros.model.Aviso
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas

@Composable
fun AvisosScreen(
    avisos: List<Aviso>,
    onVolver: () -> Unit,
    onAbrir: (Aviso) -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_avisos), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            if (avisos.isEmpty()) {
                item { Text(stringResource(R.string.sin_avisos), style = MaterialTheme.typography.bodyLarge) }
            }
            items(avisos, key = { it.id }) { aviso ->
                Card(
                    onClick = { onAbrir(aviso) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    shape = EstilosTarjeta.forma,
                    colors = CardDefaults.cardColors(
                        containerColor = if (aviso.leido) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(Modifier.padding(Dimens.espacioMedio), verticalAlignment = Alignment.Top) {
                        Icon(
                            painterResource(R.drawable.ic_campana), contentDescription = null,
                            modifier = Modifier.size(Dimens.tamanoIcono)
                        )
                        Column(Modifier.padding(start = Dimens.espacioChico)) {
                            Text(
                                (if (aviso.leido) "" else stringResource(R.string.texto_nuevo) + " · ") +
                                        "${aviso.motivo} · ${aviso.numeroSiniestro}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                aviso.mensaje,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (aviso.leido) FontWeight.Normal else FontWeight.SemiBold
                            )
                            Text(Fechas.paraMostrar(aviso.fechaHora), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
