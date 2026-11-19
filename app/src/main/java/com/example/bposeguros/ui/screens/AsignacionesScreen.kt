package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.model.AsignacionUiState
import com.example.bposeguros.model.CandidatoLiquidador
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.AvatarUsuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.DialogoCerrarSesion
import com.example.bposeguros.ui.components.TarjetaSiniestro
import com.example.bposeguros.ui.theme.Dimens

@Composable
fun AsignacionesScreen(
    usuario: Usuario,
    uiState: AsignacionUiState,
    onElegirCaso: (Siniestro) -> Unit,
    onCerrarDialogo: () -> Unit,
    onAsignar: (CandidatoLiquidador) -> Unit,
    onMensajeMostrado: () -> Unit,
    onNuevoLiquidador: () -> Unit,
    onVerLiquidadores: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var preguntarSalida by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(R.string.titulo_asignaciones)) {
                IconButton(onClick = onVerLiquidadores) {
                    Icon(painterResource(R.drawable.ic_persona), contentDescription = stringResource(R.string.cd_ver_liquidadores))
                }
                IconButton(onClick = onVerPerfil) {
                    AvatarUsuario(ruta = usuario.foto, tamano = 32.dp, conBorde = false)
                }
                IconButton(onClick = { preguntarSalida = true }) {
                    Icon(painterResource(R.drawable.ic_salir), contentDescription = stringResource(R.string.btn_cerrar_sesion))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoLiquidador,
                icon = { Icon(painterResource(R.drawable.ic_persona_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_nuevo_liquidador)) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                Text(
                    stringResource(R.string.texto_analista, usuario.primerNombre(), uiState.pendientes.size),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.pendientes.isEmpty()) {
                item { Text(stringResource(R.string.sin_pendientes)) }
            }
            items(uiState.pendientes, key = { it.id }) { caso ->
                val cliente = caso.aseguradoNombre.ifBlank { stringResource(R.string.texto_cliente_sin_nombre) } +
                        " · " + caso.rutAseguradoFormateado()
                TarjetaSiniestro(
                    siniestro = caso,
                    onClick = { onElegirCaso(caso) },
                    extra = cliente,
                    modifier = Modifier.animateItem()
                )
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    uiState.casoElegido?.let { caso ->
        var elegido by remember(caso.id) { mutableStateOf(uiState.candidatos.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = onCerrarDialogo,
            title = { Text(stringResource(R.string.titulo_asignar)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    Text(
                        stringResource(R.string.texto_asignar_caso, caso.numero, caso.tipoNombre),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.candidatos.isEmpty()) {
                        Text(
                            stringResource(R.string.texto_sin_candidatos),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.candidatos.forEachIndexed { indice, candidato ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = elegido == candidato.id,
                                    onClick = { elegido = candidato.id },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = elegido == candidato.id, onClick = null)
                            Column(modifier = Modifier.padding(start = Dimens.espacioChico)) {
                                Text(
                                    candidato.nombre + " (" + candidato.codigo + ")",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                val carga = stringResource(R.string.texto_carga_liquidador, candidato.casosAbiertos)
                                Text(
                                    if (indice == 0) carga + " · " + stringResource(R.string.etiqueta_sugerido) else carga,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { uiState.candidatos.firstOrNull { it.id == elegido }?.let(onAsignar) },
                    enabled = elegido != null
                ) { Text(stringResource(R.string.btn_asignar)) }
            },
            dismissButton = {
                TextButton(onClick = onCerrarDialogo) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    if (preguntarSalida) {
        DialogoCerrarSesion(onConfirmar = onCerrarSesion, onCancelar = { preguntarSalida = false })
    }
}