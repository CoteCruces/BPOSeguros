package com.example.bposeguros.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.components.TarjetaSiniestro
import com.example.bposeguros.ui.theme.Dimens
import kotlinx.coroutines.launch

// =====================================================================
//  MisSiniestrosScreen.kt  —  Pantalla principal del asegurado (RF-04 / HU-02)
//  Lista sus siniestros (abiertos primero), con la etapa visible de inmediato (RNF-02).
//  También pide el permiso de NOTIFICACIONES (Android 13+) para avisarle los cambios.
// =====================================================================
@Composable
fun MisSiniestrosScreen(
    usuario: Usuario,
    siniestros: List<Siniestro>,
    avisosSinLeer: Int,
    onAbrir: (Long) -> Unit,
    onReportar: () -> Unit,
    onVerAvisos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoDenegado = stringResource(R.string.permiso_notificaciones_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    // PERMISOS paso 2: VERIFICAR. Se guarda en un estado para ocultar la tarjeta al concederlo.
    var puedeNotificar by remember { mutableStateOf(Permisos.puedeNotificar(context)) }

    // PERMISOS paso 3 y 4: PEDIR y REACCIONAR
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
            BarraSuperior(titulo = stringResource(R.string.saludo, usuario.primerNombre())) {
                // Campana con un globito que cuenta los avisos sin leer
                IconButton(onClick = onVerAvisos) {
                    BadgedBox(badge = { if (avisosSinLeer > 0) Badge { Text("$avisosSinLeer") } }) {
                        Icon(painterResource(R.drawable.ic_campana), contentDescription = stringResource(R.string.titulo_avisos))
                    }
                }
                IconButton(onClick = onCerrarSesion) {
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
                // RNF-10: el RUT se muestra parcialmente oculto
                Text(
                    stringResource(R.string.texto_rut_oculto, usuario.rutOculto()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            if (siniestros.isEmpty()) {
                item {
                    Column(Modifier.padding(top = 32.dp)) {
                        Text(stringResource(R.string.sin_siniestros), style = MaterialTheme.typography.bodyLarge)  // HU-02 CA2
                    }
                }
            }
            // key = id: Compose sabe qué tarjeta es cuál.
            // ANIMACIÓN: animateItem() hace que las tarjetas se deslicen al cambiar la lista.
            items(siniestros, key = { it.id }) { siniestro ->
                TarjetaSiniestro(
                    siniestro = siniestro,
                    onClick = { onAbrir(siniestro.id) },
                    modifier = Modifier.animateItem()
                )
            }
            item { Spacer(Modifier.height(72.dp)) }     // espacio para que el botón flotante no tape la última
        }
    }
}
