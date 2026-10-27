package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.AvatarUsuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.DialogoCerrarSesion
import com.example.bposeguros.ui.components.TarjetaSiniestro
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas

@Composable
fun CasosLiquidadorScreen(
    usuario: Usuario,
    casos: List<Siniestro>,
    busqueda: String,
    onBusquedaCambia: (String) -> Unit,
    onAbrir: (Long) -> Unit,
    onNuevoCliente: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var preguntarSalida by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = stringResource(R.string.titulo_mis_casos)) {
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
                onClick = onNuevoCliente,
                icon = { Icon(painterResource(R.drawable.ic_persona_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_nuevo_cliente)) }
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
                    stringResource(R.string.texto_liquidador, usuario.nombre, usuario.codigo, usuario.equipo),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                CampoTexto(
                    valor = busqueda,
                    onValorCambia = onBusquedaCambia,
                    etiqueta = stringResource(R.string.campo_buscar_caso),
                    icono = R.drawable.ic_buscar,
                    ayuda = stringResource(R.string.ayuda_buscar_caso)
                )
            }
            if (casos.isEmpty()) {
                item {
                    Text(stringResource(if (busqueda.isBlank()) R.string.sin_casos else R.string.sin_resultados_busqueda))
                }
            }
            items(casos, key = { it.id }) { caso ->
                val dias = Fechas.diasEntre(caso.ultimaActualizacion ?: caso.fechaCambioEtapa, Fechas.hoy())
                val cliente = caso.aseguradoNombre.ifBlank { stringResource(R.string.texto_cliente_sin_nombre) } +
                        " · " + caso.rutAseguradoFormateado()
                TarjetaSiniestro(
                    siniestro = caso,
                    onClick = { onAbrir(caso.id) },
                    extra = cliente,
                    alerta = if (!caso.estaCerrado && dias > 7) stringResource(R.string.alerta_sin_movimiento, dias) else null,
                    modifier = Modifier.animateItem()
                )
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (preguntarSalida) {
        DialogoCerrarSesion(onConfirmar = onCerrarSesion, onCancelar = { preguntarSalida = false })
    }
}
