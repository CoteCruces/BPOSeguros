package com.example.bposeguros.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo
import com.example.bposeguros.viewmodel.FormularioViewModel

@Composable
fun FormularioScreen(
    onVolver: () -> Unit,
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FormularioContent(
        uiState = uiState,
        onVolver = onVolver,
        onRutCambia = viewModel::onRutCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCrear = viewModel::onCrear,
        onLimpiar = viewModel::onLimpiar
    )
}

@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onVolver: () -> Unit,
    onRutCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onCrear: () -> Unit,
    onLimpiar: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_nuevo_cliente), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro_bpo,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_nuevo_cliente),
                subtitulo = stringResource(R.string.banner_nuevo_cliente_sub)
            )
            AnimatedContent(
                targetState = uiState.cuentaCreada,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "formularioOExito"
            ) { creada ->
                Column(
                    modifier = Modifier.padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    if (!creada) {
                        TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_obligatorios)) {
                            CampoTexto(
                                valor = uiState.rut,
                                onValorCambia = onRutCambia,
                                etiqueta = stringResource(R.string.campo_rut),
                                icono = R.drawable.ic_rut,
                                error = uiState.errorRut,
                                ayuda = stringResource(R.string.ayuda_rut)
                            )
                            CampoTexto(
                                valor = uiState.correo,
                                onValorCambia = onCorreoCambia,
                                etiqueta = stringResource(R.string.campo_correo),
                                icono = R.drawable.ic_correo,
                                error = uiState.errorCorreo,
                                tipoTeclado = KeyboardType.Email
                            )
                            AnimatedVisibility(
                                visible = uiState.errorGeneral != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Text(
                                    text = uiState.errorGeneral.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            AnimatedVisibility(visible = uiState.enviando) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                            BotonSecundario(
                                texto = stringResource(R.string.btn_limpiar),
                                onClick = onLimpiar,
                                modifier = Modifier.weight(1f)

                            )
                            BotonPrincipal(
                                texto = stringResource(
                                    if (uiState.enviando) R.string.btn_enviando else R.string.btn_crear_cuenta
                                ),
                                onClick = onCrear,
                                icono = R.drawable.ic_check,
                                habilitado = !uiState.enviando,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = EstilosTarjeta.forma,
                            colors = CardDefaults.cardColors(containerColor = ExitoFondo)
                        ) {
                            Text(
                                text = stringResource(R.string.cuenta_creada, uiState.rut, uiState.correo),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Exito,
                                modifier = Modifier.padding(Dimens.espacioMedio)
                            )
                        }
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_crear_otra),
                            onClick = onLimpiar,
                            icono = R.drawable.ic_persona_agregar,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FormularioPreview() {
    BPOSegurosTheme {
        FormularioContent(
            uiState = FormularioUiState(
                rut = "15.842.371-R",
                errorRut = "El RUT no es válido. Revisa el dígito verificador",
                correo = "ana@",
                errorCorreo = "Ingresa un correo válido (ejemplo: usuario@dominio.com)"
            ),
            onVolver = {},
            onRutCambia = {},
            onCorreoCambia = {},
            onCrear = {},
            onLimpiar = {}
        )
    }
}