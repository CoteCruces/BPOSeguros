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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.InterruptorConTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.LoginViewModel

// =====================================================================
//  LoginScreen.kt — PANTALLA DE INGRESO (acceso simulado, sin contraseña)
//   1) LoginScreen  -> CON estado: conoce el ViewModel.
//   2) LoginContent -> SIN estado: recibe datos y lambdas (se ve en @Preview).
//  La pantalla NO navega: avisa con onIngresoExitoso(usuario) / onIrARegistro().
// =====================================================================
@Composable
fun LoginScreen(
    rutInicial: String,
    onIngresoExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Si se llega desde el registro, el RUT aparece ya escrito
    LaunchedEffect(rutInicial) {
        if (rutInicial.isNotBlank()) viewModel.onRutCambia(rutInicial)
    }

    LoginContent(
        uiState = uiState,
        onModoCambia = viewModel::onModoCambia,
        onRutCambia = viewModel::onRutCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        // ?.let = "si NO es null, ejecuta" -> solo navega si el usuario existe
        onIngresar = { viewModel.onIngresar()?.let(onIngresoExitoso) },
        onIrARegistro = onIrARegistro
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onModoCambia: (Boolean) -> Unit,
    onRutCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onIngresar: () -> Unit,
    onIrARegistro: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_login)) },
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
            Column (
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_ingreso)) {
                    InterruptorConTexto(
                        texto = stringResource(R.string.opcion_soy_liquidador),
                        activado = uiState.esColaborador,
                        onCambio = onModoCambia
                    )

                    // ANIMACIÓN: AnimatedContent cambia el campo RUT <-> código con un fundido
                    AnimatedContent(
                        targetState = uiState.esColaborador,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "campoIngreso"
                    ) { esColaborador ->
                        if (esColaborador) {
                            CampoTexto(
                                valor = uiState.codigo,
                                onValorCambia = onCodigoCambia,
                                etiqueta = stringResource(R.string.campo_codigo),
                                icono = R.drawable.ic_persona,
                                error = uiState.errorCodigo,
                                ayuda = stringResource(R.string.ayuda_codigo)
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
                    style = MaterialTheme.typography.bodyMedium,
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
            uiState = LoginUiState(rut = "15.842.371-4", errorRut = "El RUT no es válido. Revisa el dígito verificador"),
            onModoCambia = {},
            onRutCambia = {},
            onCodigoCambia = {},
            onIngresar = {},
            onIrARegistro = {}
        )
    }
}