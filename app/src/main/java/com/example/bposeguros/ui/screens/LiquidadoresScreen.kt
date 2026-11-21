package com.example.bposeguros.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.data.remote.HistorialResponse
import com.example.bposeguros.model.LiquidadorResumen
import com.example.bposeguros.model.LiquidadoresUiState
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo

@Composable
fun LiquidadoresScreen(
    uiState: LiquidadoresUiState,
    onVolver: () -> Unit,
    onNuevoLiquidador: () -> Unit,
    onBusquedaCambia: (String) -> Unit,
    onVerMas: () -> Unit,
    onVerCasos: (LiquidadorResumen) -> Unit,
    onCerrarCasos: () -> Unit,
    onReintentar: () -> Unit,
    onPedirCambio: (LiquidadorResumen) -> Unit,
    onContinuarCambio: () -> Unit,
    onConfirmarCambio: () -> Unit,
    onCancelarCambio: () -> Unit,
    onMensajeMostrado: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_liquidadores), onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoLiquidador,
                icon = { Icon(painterResource(R.drawable.ic_persona_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_nuevo_liquidador)) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item { Text(stringResource(R.string.texto_liquidadores), style = MaterialTheme.typography.bodyMedium) }

            item {
                CampoTexto(
                    valor = uiState.busqueda,
                    onValorCambia = onBusquedaCambia,
                    etiqueta = stringResource(R.string.liq_buscar),
                    icono = R.drawable.ic_buscar,
                    error = null
                )
            }

            if (uiState.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            val mensajeError = uiState.error
            if (mensajeError != null) {
                item {
                    Column {
                        Text(mensajeError, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onReintentar) { Text(stringResource(R.string.liq_reintentar)) }
                    }
                }
            } else if (!uiState.cargando && uiState.liquidadores.isEmpty()) {
                item {
                    Text(
                        stringResource(
                            if (uiState.busqueda.isBlank()) R.string.sin_liquidadores else R.string.liq_sin_resultados
                        )
                    )
                }
            }


            items(uiState.liquidadores.take(uiState.visibles), key = { it.id }) { liquidador ->
                FilaLiquidador(
                    liquidador = liquidador,
                    onVerCasos = { onVerCasos(liquidador) },
                    onPedirCambio = { onPedirCambio(liquidador) },
                    modifier = Modifier.animateItem()
                )
            }

            val restantes = uiState.liquidadores.size - uiState.visibles
            if (restantes > 0) {
                item {
                    TextButton(onClick = onVerMas, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.liq_ver_mas, restantes))
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }


    uiState.liquidadorConCasos?.let { liquidador ->
        val errorCasos = uiState.errorCasos
        AlertDialog(
            onDismissRequest = onCerrarCasos,
            icon = { Icon(painterResource(R.drawable.ic_maleta), contentDescription = null) },
            title = { Text(stringResource(R.string.liq_titulo_casos, liquidador.nombre)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    when {
                        uiState.cargandoCasos -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        errorCasos != null -> Text(errorCasos, color = MaterialTheme.colorScheme.error)
                        uiState.casosDelLiquidador.isEmpty() -> Text(stringResource(R.string.liq_sin_casos))
                        else -> uiState.casosDelLiquidador.forEach { caso -> FilaCaso(caso) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onCerrarCasos) { Text(stringResource(R.string.liq_cerrar)) }
            }
        )
    }


    uiState.pendienteDeCambio?.let { liquidador ->
        val bloqueando = liquidador.activo

        if (!uiState.confirmandoCambio) {
            AlertDialog(
                onDismissRequest = onCancelarCambio,
                icon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) },
                title = {
                    Text(stringResource(if (bloqueando) R.string.titulo_bloquear else R.string.titulo_habilitar, liquidador.nombre))
                },
                text = {
                    Text(
                        if (bloqueando) stringResource(R.string.texto_bloquear, liquidador.casosAbiertos)
                        else stringResource(R.string.texto_habilitar)
                    )
                },
                confirmButton = {
                    TextButton(onClick = onContinuarCambio) { Text(stringResource(R.string.liq_continuar)) }
                },
                dismissButton = {
                    TextButton(onClick = onCancelarCambio) { Text(stringResource(R.string.btn_cancelar)) }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = onCancelarCambio,
                icon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) },
                title = {
                    Text(stringResource(if (bloqueando) R.string.liq_seguro_bloquear else R.string.liq_seguro_habilitar, liquidador.nombre))
                },
                text = {
                    Text(
                        when {
                            !bloqueando -> stringResource(R.string.liq_seguro_texto_habilitar)
                            liquidador.casosAbiertos > 0 ->
                                stringResource(R.string.liq_seguro_texto_bloquear_con_casos, liquidador.casosAbiertos)
                            else -> stringResource(R.string.liq_seguro_texto_bloquear_sin_casos)
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmarCambio) {
                        Text(stringResource(if (bloqueando) R.string.liq_si_bloquear else R.string.liq_si_habilitar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelarCambio) { Text(stringResource(R.string.btn_cancelar)) }
                }
            )
        }
    }
}

@Composable
private fun FilaCaso(caso: HistorialResponse) {
    Column {
        Text("N° ${caso.numero} · ${caso.etapaEtiqueta}", style = MaterialTheme.typography.titleSmall)
        Text(
            "${caso.aseguradoNombre} · ${caso.companiaNombre} · ${caso.tipoNombre}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (caso.diasTrabajados <= 0L) stringResource(R.string.liq_asignado_hoy)
            else stringResource(R.string.liq_dias_caso, caso.diasTrabajados.toInt(), fechaCorta(caso.fechaInicio)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


private fun fechaCorta(fecha: String): String {
    val partes = fecha.take(10).split("-")
    return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else fecha
}

@Composable
private fun FilaLiquidador(
    liquidador: LiquidadorResumen,
    onVerCasos: () -> Unit,
    onPedirCambio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondoEstado by animateColorAsState(
        targetValue = if (liquidador.activo) ExitoFondo else MaterialTheme.colorScheme.errorContainer,
        label = "fondoEstado"
    )
    val textoEstado by animateColorAsState(
        targetValue = if (liquidador.activo) Exito else MaterialTheme.colorScheme.onErrorContainer,
        label = "textoEstado"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(liquidador.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.texto_codigo_equipo, liquidador.codigo, liquidador.equipo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.texto_casos_abiertos_liq, liquidador.casosAbiertos),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(if (liquidador.activo) R.string.estado_activo else R.string.estado_bloqueado),
                    style = MaterialTheme.typography.labelMedium,
                    color = textoEstado,
                    modifier = Modifier
                        .background(fondoEstado, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onVerCasos) {
                    Icon(painterResource(R.drawable.ic_maleta), contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text(stringResource(R.string.liq_ver_casos))
                }
                TextButton(onClick = onPedirCambio) {
                    Text(stringResource(if (liquidador.activo) R.string.btn_bloquear else R.string.btn_habilitar))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun LiquidadoresPreview() {
    BPOSegurosTheme {
        LiquidadoresScreen(
            uiState = LiquidadoresUiState(
                liquidadores = listOf(
                    LiquidadorResumen(9, "Paulina Fernández Ruiz", "LIQ-01", "Vehículos", true, 2),
                    LiquidadorResumen(13, "Andrés Bravo Medina", "LIQ-05", "Vehículos", false, 0)
                )
            ),
            onVolver = {},
            onNuevoLiquidador = {},
            onBusquedaCambia = {},
            onVerMas = {},
            onVerCasos = {},
            onCerrarCasos = {},
            onReintentar = {},
            onPedirCambio = {},
            onContinuarCambio = {},
            onConfirmarCambio = {},
            onCancelarCambio = {},
            onMensajeMostrado = {}
        )
    }
}