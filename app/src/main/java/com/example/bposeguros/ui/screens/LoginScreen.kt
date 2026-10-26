package com.example.bposeguros.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.model.LoginUiState
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoContrasena
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.InterruptorConTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    rutInicial: String,
    onIngresoExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
    onOlvideClave: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(rutInicial) {
        if (rutInicial.isNotBlank()) viewModel.onRutCambia(rutInicial)
    }

    LoginContent(
        uiState = uiState,
        onModoCambia = viewModel::onModoCambia,
        onRutCambia = viewModel::onRutCambia,
        onContrasenaCambia = viewModel::onCodigoCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = { viewModel.onIngresar()?.let(onIngresoExitoso) },
        onIrARegistro = onIrARegistro,
        onOlvideClave = onOlvideClave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onModoCambia: (Boolean) -> Unit,
    onRutCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onIrARegistro: () -> Unit,
    onOlvideClave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_bienvenida)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ImagenEncabezado(
                imagen = R.drawable.portada_bpo,
                descripcion = stringResource(R.string.cd_portada),
                titulo = stringResource(R.string.banner_login_titulo),
                subtitulo = stringResource(R.string.banner_login_subtitulo)
            )
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_ingreso)) {
                    InterruptorConTexto(
                        texto = stringResource(R.string.opcion_soy_liquidador),
                        activado = uiState.esLiquidador,
                        onCambio = onModoCambia
                    )

                    Text(
                        text = if (uiState.esLiquidador) {
                            stringResource(R.string.texto_ingreso_liquidador)
                        } else {
                            stringResource(R.string.texto_ingreso_cliente)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    AnimatedContent(
                        targetState = uiState.esLiquidador,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "campoIngreso"
                    ) { esColaborador ->
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                            if (esColaborador) {
                                CampoTexto(
                                    valor = uiState.rut,
                                    onValorCambia = onRutCambia,
                                    etiqueta = stringResource(R.string.campo_rut_liquidador),
                                    icono = R.drawable.ic_rut,
                                    error = uiState.errorRut,
                                    ayuda = stringResource(R.string.ayuda_rut_liquidador)
                                )
                            } else {
                                CampoTexto(
                                    valor = uiState.rut,
                                    onValorCambia = onRutCambia,
                                    etiqueta = stringResource(R.string.campo_rut),
                                    icono = R.drawable.ic_rut,
                                    error = uiState.errorRut,
                                    ayuda = stringResource(R.string.ayuda_rut),
                                    tipoTeclado = KeyboardType.Text
                                )
                            }

                            CampoContrasena(
                                valor = uiState.clave,
                                onValorCambia = onContrasenaCambia,
                                etiqueta = stringResource(R.string.campo_contrasena),
                                visible = uiState.claveVisible,
                                onAlternarVisible = onAlternarVisibilidad,
                                error = uiState.errorCodigo
                            )
                        }
                    }

                    TextButton(
                        onClick = onOlvideClave,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_olvide_clave),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    BotonPrincipal(
                        texto = stringResource(R.string.btn_ingresar),
                        onClick = onIngresar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_ir_registro),
                        onClick = onIrARegistro,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = stringResource(R.string.texto_datos_prueba),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginPreview() {
    BPOSegurosTheme {
        LoginContent(
            uiState = LoginUiState(
                rut = "15.842.371-5",
                errorRut = null
            ),
            onModoCambia = {},
            onRutCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onIrARegistro = {},
            onOlvideClave = {}
        )
    }
}