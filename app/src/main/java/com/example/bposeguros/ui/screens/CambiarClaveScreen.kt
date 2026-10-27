package com.example.bposeguros.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.CampoContrasena
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.CambiarClaveViewModel

// =====================================================================
//  CambiarClaveScreen.kt  —  Cambiar la contraseña
//  obligatorio = true  -> entró con una contraseña temporal: debe cambiarla para seguir.
//                         La flecha de volver CIERRA LA SESIÓN (no puede saltarse este paso).
//  obligatorio = false -> viene desde "Mi perfil": la flecha vuelve al perfil.
// =====================================================================
@Composable
fun CambiarClaveScreen(
    idUsuario: Long,
    obligatorio: Boolean,
    onVolver: () -> Unit,
    onCambiada: () -> Unit,
    viewModel: CambiarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el ViewModel avisa "cambiada", AppNavegacion decide a dónde ir
    LaunchedEffect(uiState.cambiada) { if (uiState.cambiada) onCambiada() }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_cambiar_clave), onVolver = onVolver) },
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
            Text(
                stringResource(if (obligatorio) R.string.texto_clave_obligatoria else R.string.texto_clave_voluntaria),
                style = MaterialTheme.typography.bodyLarge
            )
            TarjetaSeccion(titulo = stringResource(R.string.seccion_contrasena)) {
                CampoContrasena(
                    valor = uiState.actual,
                    onValorCambia = viewModel::onActualCambia,
                    etiqueta = stringResource(
                        if (obligatorio) R.string.campo_clave_temporal else R.string.campo_clave_actual
                    ),
                    visible = uiState.visible,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = uiState.errorActual
                )
                CampoContrasena(
                    valor = uiState.nueva,
                    onValorCambia = viewModel::onNuevaCambia,
                    etiqueta = stringResource(R.string.campo_clave_nueva),
                    visible = uiState.visible,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = uiState.errorNueva
                )
                CampoContrasena(
                    valor = uiState.repetir,
                    onValorCambia = viewModel::onRepetirCambia,
                    etiqueta = stringResource(R.string.campo_clave_repetir),
                    visible = uiState.visible,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = uiState.errorRepetir
                )
                Text(
                    stringResource(R.string.ayuda_reglas_clave),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_clave),
                onClick = { viewModel.onGuardar(idUsuario) },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
