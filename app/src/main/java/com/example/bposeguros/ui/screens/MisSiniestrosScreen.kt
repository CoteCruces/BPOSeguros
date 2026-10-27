package com.example.bposeguros.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.permisos.Permisos
import com.example.bposeguros.ui.components.AvatarUsuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.DialogoCerrarSesion
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.components.TarjetaSiniestro
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    usuario: Usuario,
    ultimos: List<Siniestro>,
    avisosSinLeer: Int,
    onAbrir: (Long) -> Unit,
    onReportar: () -> Unit,
    onVerAvisos: () -> Unit,
    onVerPerfil: () -> Unit,
    onBuscarPorFechas: (String, String) -> Unit,
    onVerTodos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoDenegado = stringResource(R.string.permiso_notificaciones_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    var mostrarCalendario by remember { mutableStateOf(false) }
    var preguntarSalida by remember { mutableStateOf(false) }

    var puedeNotificar by remember { mutableStateOf(Permisos.puedeNotificar(context)) }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        puedeNotificar = concedido
        if (!concedido) {
            scope.launch {
                val r = snackbarHostState.showSnackbar(textoDenegado, textoAjustes, duration = SnackbarDuration.Long)
                if (r == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
            }
        }
    }

    Scaffold(
        topBar = {
            val saludo = if (usuario.nombre.isBlank()) stringResource(R.string.saludo_sin_nombre)
                         else stringResource(R.string.saludo, usuario.primerNombre())
            BarraSuperior(titulo = saludo) {
                IconButton(onClick = onVerAvisos) {
                    BadgedBox(badge = { if (avisosSinLeer > 0) Badge { Text("$avisosSinLeer") } }) {
                        Icon(painterResource(R.drawable.ic_campana), contentDescription = stringResource(R.string.titulo_avisos))
                    }
                }
                IconButton(onClick = onVerPerfil) {
                    AvatarUsuario(ruta = usuario.foto, tamano = 32.dp, conBorde = false)
                }
                IconButton(onClick = { preguntarSalida = true }) {
                    Icon(painterResource(R.drawable.ic_salir), contentDescription = stringResource(R.string.btn_cerrar_sesion))
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onReportar,
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_reportar)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    stringResource(R.string.texto_rut_oculto, usuario.rutOculto()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (usuario.nombre.isBlank()) {
                item {
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_completa_perfil)) {
                        Text(stringResource(R.string.texto_completa_perfil), style = MaterialTheme.typography.bodyMedium)
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_ir_perfil),
                            onClick = onVerPerfil,
                            icono = R.drawable.ic_persona
                        )
                    }
                }
            }
            if (!puedeNotificar) {
                item {
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_activar_avisos)) {
                        Text(stringResource(R.string.texto_activar_avisos), style = MaterialTheme.typography.bodyMedium)
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_activar_avisos),
                            onClick = { pedirPermiso.launch(Permisos.NOTIFICACIONES) },
                            icono = R.drawable.ic_campana
                        )
                    }
                }
            }

            item {
                Text(stringResource(R.string.titulo_ultimos), style = MaterialTheme.typography.titleMedium)
            }
            if (ultimos.isEmpty()) {
                item {
                    Text(stringResource(R.string.sin_siniestros), style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(ultimos, key = { it.id }) { siniestro ->
                TarjetaSiniestro(
                    siniestro = siniestro,
                    onClick = { onAbrir(siniestro.id) },
                    modifier = Modifier.animateItem()
                )
            }

            item {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_buscar_siniestros)) {
                    Text(stringResource(R.string.texto_buscar_fecha), style = MaterialTheme.typography.bodyMedium)
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_buscar_fecha),
                        onClick = { mostrarCalendario = true },
                        icono = R.drawable.ic_calendario,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_ver_todos),
                        onClick = onVerTodos,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (mostrarCalendario) {
        val estadoCalendario = rememberDateRangePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val inicio = estadoCalendario.selectedStartDateMillis
                        val fin = estadoCalendario.selectedEndDateMillis ?: inicio
                        if (inicio != null && fin != null) {
                            onBuscarPorFechas(Fechas.desdeMilisegundos(inicio), Fechas.desdeMilisegundos(fin))
                        }
                        mostrarCalendario = false
                    },
                    enabled = estadoCalendario.selectedStartDateMillis != null
                ) { Text(stringResource(R.string.btn_buscar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        ) {
            DateRangePicker(state = estadoCalendario, modifier = Modifier.weight(1f))
        }
    }

    if (preguntarSalida) {
        DialogoCerrarSesion(onConfirmar = onCerrarSesion, onCancelar = { preguntarSalida = false })
    }
}
