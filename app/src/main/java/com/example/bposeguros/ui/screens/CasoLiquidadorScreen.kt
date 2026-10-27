package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bposeguros.R
import com.example.bposeguros.model.DialogoLiquidador
import com.example.bposeguros.model.TipoDocumento
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoSeleccion
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.InterruptorConTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.LiquidadorViewModel

@Composable
fun CasoLiquidadorScreen(
    idSiniestro: Long,
    liquidador: Usuario,
    viewModel: LiquidadorViewModel,
    onVolver: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val acciones by viewModel.acciones.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idSiniestro) { viewModel.cargarDetalle(idSiniestro, liquidador.id) }

    LaunchedEffect(acciones.mensaje) {
        acciones.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = detalle.siniestro?.numero ?: "", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val siniestro = detalle.siniestro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            if (siniestro == null) {
                Text(stringResource(R.string.caso_no_asignado))
                return@Column
            }
            SeccionEtapa(siniestro, hayPendientes = detalle.pendientes > 0)

            if (!siniestro.estaCerrado) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_acciones)) {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_registrar_gestion),
                        onClick = { viewModel.abrirDialogo(DialogoLiquidador.GESTION) },
                        icono = R.drawable.ic_editar,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_pedir_documento),
                        onClick = { viewModel.abrirDialogo(DialogoLiquidador.SOLICITUD) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (siniestro.etapa.siguiente() != null) {
                        BotonSecundario(
                            texto = stringResource(R.string.btn_avanzar, stringResource(siniestro.etapa.siguiente()!!.etiqueta)),
                            onClick = { viewModel.avanzarEtapa(liquidador) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BotonSecundario(
                        texto = stringResource(R.string.btn_cerrar_caso),
                        onClick = { viewModel.abrirDialogo(DialogoLiquidador.CIERRE) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            SeccionDocumentos(detalle.solicitudes, detalle.adjuntos) {}
            SeccionDatos(siniestro)
            SeccionHistorial(detalle.gestiones, mostrarVisibilidad = true)
        }
    }

    val siniestro = detalle.siniestro ?: return
    when (acciones.dialogo) {
        DialogoLiquidador.GESTION -> DialogoFormulario(
            titulo = stringResource(R.string.btn_registrar_gestion),
            onConfirmar = { viewModel.registrarGestion(liquidador) },
            onCancelar = viewModel::cerrarDialogo
        ) {
            CampoTexto(
                valor = acciones.descripcionGestion,
                onValorCambia = viewModel::onDescripcionGestion,
                etiqueta = stringResource(R.string.campo_que_hiciste),
                icono = R.drawable.ic_editar,
                error = acciones.errorGestion,
                ayuda = stringResource(R.string.ayuda_lenguaje_simple),
                lineas = 3
            )
            InterruptorConTexto(
                texto = stringResource(R.string.opcion_visible_asegurado),
                activado = acciones.gestionVisible,
                onCambio = viewModel::onGestionVisible
            )
        }

        DialogoLiquidador.SOLICITUD -> DialogoFormulario(
            titulo = stringResource(R.string.btn_pedir_documento),
            onConfirmar = { viewModel.solicitarDocumento(liquidador) },
            onCancelar = viewModel::cerrarDialogo
        ) {
            CampoSeleccion(
                etiqueta = stringResource(R.string.campo_tipo_documento),
                opciones = TipoDocumento.lista,
                seleccionada = acciones.documentoElegido,
                onSeleccionar = viewModel::onDocumentoElegido,
                icono = R.drawable.ic_documento,
                error = acciones.errorDocumento
            )
            CampoTexto(
                valor = acciones.comentarioSolicitud,
                onValorCambia = viewModel::onComentarioSolicitud,
                etiqueta = stringResource(R.string.campo_comentario),
                icono = R.drawable.ic_editar,
                ayuda = stringResource(R.string.ayuda_opcional),
                lineas = 2
            )
        }

        DialogoLiquidador.CIERRE -> {
            val permitidos = siniestro.etapa.resultadosPermitidos()
            DialogoFormulario(
                titulo = stringResource(R.string.btn_cerrar_caso),
                onConfirmar = { viewModel.cerrarCaso(liquidador) },
                onCancelar = viewModel::cerrarDialogo
            ) {
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_resultado),
                    opciones = permitidos.map { stringResource(it.etiqueta) },
                    seleccionada = acciones.resultadoElegido,
                    onSeleccionar = viewModel::onResultadoElegido,
                    icono = R.drawable.ic_check,
                    error = acciones.errorResultado
                )
                CampoTexto(
                    valor = acciones.mensajeCierre,
                    onValorCambia = viewModel::onMensajeCierre,
                    etiqueta = stringResource(R.string.campo_mensaje_cierre),
                    icono = R.drawable.ic_editar,
                    error = acciones.errorMensaje,
                    ayuda = stringResource(R.string.ayuda_lenguaje_simple),
                    lineas = 3
                )
            }
        }

        null -> {}
    }
}

@Composable
private fun DialogoFormulario(
    titulo: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    contenido: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) { contenido() } },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(stringResource(R.string.btn_guardar)) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.btn_cancelar)) } }
    )
}
