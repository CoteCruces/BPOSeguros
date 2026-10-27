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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.CampoContrasena
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.InterruptorConTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.LoginViewModel

// =====================================================================
//  LoginScreen.kt — PANTALLA DE BIENVENIDA (primera pantalla de la app)
//  Da la bienvenida y permite entrar con RUT + contraseña.
//  Tiene el interruptor "Soy liquidador de Andes" del proyecto original: al moverlo,
//  AnimatedContent cambia el texto y el campo con un fundido (animación pedida en la rúbrica).
//  Ya NO tiene "Registrar mis datos": las cuentas de clientes las crea el liquidador.
//   1) LoginScreen  -> CON estado: conoce el ViewModel.
//   2) LoginContent -> SIN estado: recibe datos y lambdas (se ve en @Preview).
//  La pantalla NO navega: avisa con onIngresoExitoso(usuario) / onOlvideClave().
// =====================================================================
@Composable
fun LoginScreen(
    onIngresoExitoso: (Usuario) -> Unit,
    onOlvideClave: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        onModoCambia = viewModel::onModoCambia,
        onRutCambia = viewModel::onRutCambia,
        onClaveCambia = viewModel::onClaveCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        // ?.let = "si NO es null, ejecuta" -> solo navega si el RUT y la clave son correctos
        onIngresar = { viewModel.onIngresar()?.let(onIngresoExitoso) },
        onOlvideClave = onOlvideClave
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onModoCambia: (Boolean) -> Unit,
    onRutCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onOlvideClave: () -> Unit
) {
    Scaffold(
        // Primera pantalla: no hay a dónde volver, por eso no lleva flecha
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_bienvenida)) },
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
                titulo = stringResource(R.string.banner_bienvenida_titulo),
                subtitulo = stringResource(R.string.banner_bienvenida_subtitulo)
            )
            Column (
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_ingreso)) {
                    InterruptorConTexto(
                        texto = stringResource(R.string.opcion_soy_liquidador),
                        activado = uiState.esLiquidador,
                        onCambio = onModoCambia
                    )

                    // ANIMACIÓN: AnimatedContent cambia el contenido de cliente <-> liquidador con un fundido.
                    // targetState = el valor que "vigila"; cada vez que cambia, anima de lo viejo a lo nuevo.
                    // Ambos escriben su RUT: cambia el texto de bienvenida, la etiqueta y el ejemplo.
                    AnimatedContent(
                        targetState = uiState.esLiquidador,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "campoIngreso"
                    ) { esLiquidador ->
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            Text(
                                text = stringResource(if (esLiquidador) R.string.texto_ingreso_liquidador else R.string.texto_ingreso_cliente),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CampoTexto(
                                valor = uiState.rut,
                                onValorCambia = onRutCambia,
                                etiqueta = stringResource(if (esLiquidador) R.string.campo_rut_liquidador else R.string.campo_rut),
                                icono = if (esLiquidador) R.drawable.ic_persona else R.drawable.ic_rut,
                                error = uiState.errorRut,
                                ayuda = stringResource(if (esLiquidador) R.string.ayuda_rut_liquidador else R.string.ayuda_rut),
                                tipoTeclado = KeyboardType.Text
                            )
                        }
                    }
                    CampoContrasena(
                        valor = uiState.clave,
                        onValorCambia = onClaveCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.claveVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorClave
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_ingresar),
                        onClick = onIngresar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // "¿Olvidaste tu contraseña?" (pedido por el profesor)
                    TextButton(onClick = onOlvideClave, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(R.string.btn_olvide_clave))
                    }
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
            uiState = LoginUiState(rut = "15.842.371-5", errorClave = "RUT o contraseña incorrectos"),
            onModoCambia = {},
            onRutCambia = {},
            onClaveCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onOlvideClave = {}
        )
    }
}
