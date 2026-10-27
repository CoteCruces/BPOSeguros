package com.example.bposeguros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.bposeguros.viewmodel.RecuperarClaveViewModel

// =====================================================================
//  RecuperarClaveScreen.kt  —  "¿Olvidaste tu contraseña?"
//  Pide RUT + correo de la cuenta. Si coinciden, muestra el correo que se
//  "envió" con la contraseña temporal (simulado: no hay servidor de correo).
// =====================================================================
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
                    valor = uiState.rut,
                    onValorCambia = viewModel::onRutCambia,
                    etiqueta = stringResource(R.string.campo_rut),
                    icono = R.drawable.ic_rut,
                    error = uiState.errorRut,
                    ayuda = stringResource(R.string.ayuda_rut)
                )
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = viewModel::onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_enviar_clave),
                    onClick = viewModel::onEnviar,
                    icono = R.drawable.ic_correo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ANIMACIÓN: el "correo enviado" aparece desplegándose
            AnimatedVisibility(visible = uiState.claveTemporal != null, enter = expandVertically() + fadeIn()) {
                CorreoSimulado(
                    para = uiState.correo,
                    asunto = stringResource(R.string.correo_recuperar_asunto),
                    cuerpo = stringResource(R.string.correo_recuperar_cuerpo, uiState.claveTemporal ?: "")
                )
            }

            if (uiState.claveTemporal != null) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_volver_ingresar),
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Tarjeta que imita un correo: Para, Asunto y el texto. La usan "Recuperar" y "Nuevo cliente".
@Composable
fun CorreoSimulado(para: String, asunto: String, cuerpo: String, titulo: String = stringResource(R.string.titulo_correo_simulado)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.correo_para, para), style = MaterialTheme.typography.bodyMedium)
            Text(asunto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(cuerpo, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
