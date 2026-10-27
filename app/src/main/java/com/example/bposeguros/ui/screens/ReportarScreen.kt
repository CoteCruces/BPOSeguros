package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoSeleccion
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.ReportarViewModel

// =====================================================================
//  ReportarScreen.kt  —  FORMULARIO "REPORTAR UN SINIESTRO" (RF-02, RF-03 / HU-01)
//  Cada campo tiene ícono y su propio mensaje de error (las reglas están en el ViewModel).
//  Dos diálogos: advertencia de posible duplicado y número de siniestro entregado.
// =====================================================================
@Composable
fun ReportarScreen(
    idAsegurado: Long,
    onVolver: () -> Unit,
    onTerminar: () -> Unit,
    viewModel: ReportarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_reportar), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(stringResource(R.string.texto_intro_reporte), style = MaterialTheme.typography.bodyLarge)

            TarjetaSeccion(titulo = stringResource(R.string.seccion_que_paso)) {
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_compania),
                    opciones = uiState.companias.map { it.nombre },
                    seleccionada = uiState.companiaElegida,
                    onSeleccionar = viewModel::onCompaniaElegida,
                    icono = R.drawable.ic_compania,
                    error = uiState.errorCompania
                )
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_tipo_siniestro),
                    opciones = uiState.tipos.map { it.nombre },
                    seleccionada = uiState.tipoElegido,
                    onSeleccionar = viewModel::onTipoElegido,
                    icono = R.drawable.ic_documento,
                    error = uiState.errorTipo,
                    // hasta elegir compañía no se sabe qué tipos corresponden (no todas tienen vehículo y hogar)
                    habilitado = uiState.companiaElegida != null
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_cuando_donde)) {
                CampoTexto(
                    valor = uiState.fechaOcurrencia,
                    onValorCambia = viewModel::onFechaCambia,
                    etiqueta = stringResource(R.string.campo_fecha_ocurrencia),
                    icono = R.drawable.ic_calendario,
                    error = uiState.errorFecha,
                    ayuda = stringResource(R.string.ayuda_fecha),
                    tipoTeclado = KeyboardType.Number
                )
                // Lista de comunas de la BD: no se puede escribir una comuna inventada
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_comuna),
                    opciones = uiState.comunas,
                    // indexOf da -1 si todavía no elige ninguna; takeIf lo convierte en null
                    seleccionada = uiState.comunas.indexOf(uiState.comuna).takeIf { it >= 0 },
                    onSeleccionar = viewModel::onComunaElegida,
                    icono = R.drawable.ic_ubicacion,
                    error = uiState.errorComuna
                )
                CampoTexto(
                    valor = uiState.descripcion,
                    onValorCambia = viewModel::onDescripcionCambia,
                    etiqueta = stringResource(R.string.campo_descripcion),
                    icono = R.drawable.ic_editar,
                    error = uiState.errorDescripcion,
                    ayuda = stringResource(R.string.ayuda_descripcion, uiState.descripcion.length),
                    lineas = 4
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_cancelar),
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_enviar),
                    onClick = { viewModel.onEnviar(idAsegurado) },
                    icono = R.drawable.ic_check,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Aclaración 3: posible duplicado -> se advierte, pero se puede continuar
    uiState.posibleDuplicado?.let { numero ->
        AlertDialog(
            onDismissRequest = viewModel::onCancelarDuplicado,
            icon = { Icon(painterResource(R.drawable.ic_advertencia), contentDescription = null) },
            title = { Text(stringResource(R.string.titulo_duplicado)) },
            text = { Text(stringResource(R.string.texto_duplicado, numero)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEnviar(idAsegurado, confirmado = true) }) {
                    Text(stringResource(R.string.btn_es_otro))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarDuplicado) { Text(stringResource(R.string.btn_revisar)) }
            }
        )
    }

    // RF-03: al terminar se entrega el número de siniestro
    uiState.numeroCreado?.let { numero ->
        AlertDialog(
            onDismissRequest = onTerminar,
            icon = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
            title = { Text(stringResource(R.string.titulo_reporte_ok)) },
            text = { Text(stringResource(R.string.reporte_exitoso, numero)) },
            confirmButton = {
                TextButton(onClick = onTerminar) { Text(stringResource(R.string.btn_ver_mis_siniestros)) }
            }
        )
    }
}
