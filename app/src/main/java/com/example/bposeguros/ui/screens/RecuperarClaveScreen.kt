package com.example.bposeguros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo
import com.example.bposeguros.viewmodel.RecuperarClaveViewModel

@Composable
fun RecuperarClaveScreen(
    onVolver: () -> Unit,
    viewModel: RecuperarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_recuperar), onVolver = onVolver) },
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
            Text(stringResource(R.string.texto_recuperar), style = MaterialTheme.typography.bodyLarge)

            TarjetaSeccion(titulo = stringResource(R.string.seccion_tu_cuenta)) {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = viewModel::onCorreoCambia,
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
                BotonPrincipal(
                    texto = stringResource(if (uiState.enviando) R.string.btn_enviando else R.string.btn_enviar_clave),
                    onClick = viewModel::onEnviar,
                    icono = R.drawable.ic_correo,
                    habilitado = !uiState.enviando,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = uiState.solicitudEnviada, enter = expandVertically() + fadeIn()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EstilosTarjeta.forma,
                    colors = CardDefaults.cardColors(containerColor = ExitoFondo)
                ) {
                    Text(
                        text = stringResource(R.string.recuperar_solicitud_enviada),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Exito,
                        modifier = Modifier.padding(Dimens.espacioMedio)
                    )
                }
            }

            if (uiState.solicitudEnviada) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_volver_ingresar),
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}