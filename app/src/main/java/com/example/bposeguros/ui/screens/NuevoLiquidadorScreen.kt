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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.bposeguros.model.Equipo
import com.example.bposeguros.model.NuevoLiquidadorUiState
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoDesplegable
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo
import com.example.bposeguros.viewmodel.NuevoLiquidadorViewModel


@Composable
fun NuevoLiquidadorScreen(
    analista: Usuario,
    onVolver: () -> Unit,
    viewModel: NuevoLiquidadorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NuevoLiquidadorContent(
        uiState = uiState,
        onVolver = onVolver,
        onRutCambia = viewModel::onRutCambia,
        onNombreCambia = viewModel::onNombreCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onEquipoElegido = viewModel::onEquipoElegido,
        onCrear = { viewModel.onCrear(analista) },
        onLimpiar = viewModel::onLimpiar
    )
}

@Composable
fun NuevoLiquidadorContent(
    uiState: NuevoLiquidadorUiState,
    onVolver: () -> Unit,
    onRutCambia: (String) -> Unit,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onEquipoElegido: (Int) -> Unit,
    onCrear: () -> Unit,
    onLimpiar: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_nuevo_liquidador), onVolver = onVolver) },
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
                titulo = stringResource(R.string.banner_nuevo_liquidador),
                subtitulo = stringResource(R.string.banner_nuevo_liquidador_sub)
            )
            AnimatedContent(
                targetState = uiState.liquidadorCreado,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "formularioOExitoLiquidador"
            ) { creado ->
                Column(
                    modifier = Modifier.padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    if (!creado) {
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
                                valor = uiState.nombre,
                                onValorCambia = onNombreCambia,
                                etiqueta = stringResource(R.string.campo_nombre_completo),
                                icono = R.drawable.ic_persona,
                                error = uiState.errorNombre,
                                ayuda = stringResource(R.string.ayuda_nombre_completo)
                            )
                            CampoTexto(
                                valor = uiState.correo,
                                onValorCambia = onCorreoCambia,
                                etiqueta = stringResource(R.string.campo_correo),
                                icono = R.drawable.ic_correo,
                                error = uiState.errorCorreo,
                                ayuda = stringResource(R.string.ayuda_correo_liquidador),
                                tipoTeclado = KeyboardType.Email
                            )
                            CampoDesplegable(
                                valor = uiState.equipoElegido?.let { Equipo.lista[it] }.orEmpty(),
                                etiqueta = stringResource(R.string.campo_equipo),
                                opciones = Equipo.lista,
                                onElegir = onEquipoElegido
                            )
                            AnimatedVisibility(
                                visible = uiState.errorEquipo != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Text(
                                    text = uiState.errorEquipo.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
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
                                    if (uiState.enviando) R.string.btn_enviando else R.string.btn_crear_liquidador
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
                                text = stringResource(
                                    R.string.liquidador_creado, uiState.nombre.trim(), uiState.codigoCreado, uiState.correo
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Exito,
                                modifier = Modifier.padding(Dimens.espacioMedio)
                            )
                        }
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_crear_otro_liquidador),
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

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun NuevoLiquidadorPreview() {
    BPOSegurosTheme {
        NuevoLiquidadorContent(
            uiState = NuevoLiquidadorUiState(
                rut = "15.842.371-4",
                errorRut = "El RUT no es válido. Revisa el dígito verificador",
                nombre = "Pedro",
                errorNombre = "Escribe nombre y apellido",
                correo = "pedro@",
                errorCorreo = "Ingresa un correo válido (ejemplo: usuario@dominio.com)",
                errorEquipo = "Elige el equipo del liquidador"
            ),
            onVolver = {},
            onRutCambia = {},
            onNombreCambia = {},
            onCorreoCambia = {},
            onEquipoElegido = {},
            onCrear = {},
            onLimpiar = {}
        )
    }
}