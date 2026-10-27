package com.example.bposeguros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.FilaDato
import com.example.bposeguros.ui.components.IndicadorEtapas
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.EtapaCerrado
import com.example.bposeguros.ui.theme.EtapaEvaluacion
import com.example.bposeguros.ui.theme.TextoOscuro
import com.example.bposeguros.ui.util.Fechas

// =====================================================================
//  DetalleSiniestroScreen.kt  —  Detalle para el ASEGURADO (RF-05, RF-06, RF-08 / HU-03, HU-04, HU-06)
//  Orden pensado para alguien estresado: 1° la etapa, 2° qué viene ahora,
//  3° documentos (lo que tiene que hacer), 4° datos, 5° historial.
//  Las secciones (SeccionDocumentos, SeccionHistorial...) son públicas porque
//  la pantalla del liquidador las reutiliza.
// =====================================================================
@Composable
fun DetalleSiniestroScreen(
    detalle: DetalleUiState,
    onVolver: () -> Unit,
    onAdjuntar: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_detalle), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val siniestro = detalle.siniestro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            if (siniestro == null) {
                Text(stringResource(R.string.siniestro_no_encontrado))
                return@Column
            }
            SeccionEtapa(siniestro, hayPendientes = detalle.pendientes > 0)
            SeccionDocumentos(
                solicitudes = detalle.solicitudes,
                adjuntos = detalle.adjuntos
            ) {
                if (siniestro.estaCerrado) {
                    Text(stringResource(R.string.texto_no_adjuntar_cerrado), style = MaterialTheme.typography.bodyMedium)
                } else {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_adjuntar),
                        onClick = onAdjuntar,
                        icono = R.drawable.ic_camara,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            SeccionDatos(siniestro)
            SeccionHistorial(detalle.gestiones, mostrarVisibilidad = false)
        }
    }
}

// --------------------------- ETAPA + PRÓXIMO PASO ---------------------------
@Composable
fun SeccionEtapa(siniestro: Siniestro, hayPendientes: Boolean) {
    TarjetaSeccion(titulo = siniestro.numero) {
        IndicadorEtapas(siniestro.etapa)
        // Aclaración 2: el próximo paso, en lenguaje simple (RNF-03)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(Dimens.espacioMedio)) {
                Text(
                    stringResource(R.string.seccion_proximo_paso),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(siniestro.proximoPaso(hayPendientes)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        // RN-02: si está cerrado se ve el resultado y el mensaje final
        if (siniestro.estaCerrado && siniestro.resultado != null) {
            Text(
                stringResource(R.string.texto_resultado, stringResource(siniestro.resultado.etiqueta)),
                style = MaterialTheme.typography.titleMedium
            )
            siniestro.mensajeCierre?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        } else {
            // RF-15 (deseable): días que lleva en la etapa actual (RN-17: días corridos)
            val dias = Fechas.diasEntre(siniestro.fechaCambioEtapa, Fechas.hoy())
            FilaDato(R.drawable.ic_reloj, stringResource(R.string.texto_dias_etapa, dias))
        }
    }
}

// --------------------------- DATOS DEL SINIESTRO ---------------------------
@Composable
fun SeccionDatos(siniestro: Siniestro) {
    TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_siniestro)) {
        FilaDato(R.drawable.ic_compania, siniestro.companiaNombre)                 // S-01
        FilaDato(R.drawable.ic_documento, siniestro.tipoNombre)
        FilaDato(R.drawable.ic_calendario, stringResource(R.string.texto_fecha_ocurrencia, Fechas.paraMostrar(siniestro.fechaOcurrencia)))
        FilaDato(R.drawable.ic_calendario, stringResource(R.string.texto_fecha_reporte, Fechas.paraMostrar(siniestro.fechaReporte)))
        FilaDato(R.drawable.ic_ubicacion, siniestro.comuna)
        // RN-14 / HU-03 CA2: nombre de pila + inicial y equipo, o "a la espera de asignación"
        FilaDato(R.drawable.ic_persona, siniestro.liquidadorVisible() ?: stringResource(R.string.texto_sin_liquidador))
        if (siniestro.fueraDePlazo) FilaDato(R.drawable.ic_advertencia, stringResource(R.string.texto_fuera_plazo))  // RN-05
        if (siniestro.descripcion.isNotBlank()) {
            Text("“${siniestro.descripcion}”", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// --------------------------- DOCUMENTOS (RF-08) ---------------------------
// "accion" = slot para el botón (el asegurado adjunta; el liquidador pide documentos)
@Composable
fun SeccionDocumentos(
    solicitudes: List<SolicitudDocumento>,
    adjuntos: List<DocumentoAdjunto>,
    accion: @Composable () -> Unit
) {
    TarjetaSeccion(titulo = stringResource(R.string.seccion_documentos)) {
        if (solicitudes.isNotEmpty()) {
            Text(stringResource(R.string.subtitulo_pedidos), style = MaterialTheme.typography.labelLarge)
            solicitudes.forEach { FilaSolicitud(it) }
            HorizontalDivider()
        }
        Text(stringResource(R.string.subtitulo_enviados, adjuntos.size), style = MaterialTheme.typography.labelLarge)
        if (adjuntos.isEmpty()) Text(stringResource(R.string.texto_sin_adjuntos), style = MaterialTheme.typography.bodyMedium)
        adjuntos.forEach { FilaAdjunto(it) }
        accion()
    }
}

// Pendiente / Entregado se distingue con ícono + texto + color (no solo color)
@Composable
private fun FilaSolicitud(solicitud: SolicitudDocumento) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(solicitud.tipoDocumento, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.texto_pedido_el, Fechas.paraMostrar(solicitud.fechaSolicitud)),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            modifier = Modifier
                .background(if (solicitud.entregado) EtapaCerrado else EtapaEvaluacion, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(if (solicitud.entregado) R.drawable.ic_check else R.drawable.ic_reloj),
                contentDescription = null, tint = TextoOscuro, modifier = Modifier.size(16.dp)
            )
            Text(
                stringResource(if (solicitud.entregado) R.string.documento_entregado else R.string.documento_pendiente),
                color = TextoOscuro, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun FilaAdjunto(adjunto: DocumentoAdjunto) {
    // remember(ruta): la imagen se lee del archivo una sola vez, no en cada dibujo
    val foto = remember(adjunto.rutaArchivo) { ConvertidorImagen.leer(adjunto.rutaArchivo)?.asImageBitmap() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (foto != null) {
            Image(
                bitmap = foto, contentDescription = adjunto.tipoDocumento, contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Icon(painterResource(R.drawable.ic_documento), contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Column(Modifier.padding(start = Dimens.espacioChico)) {
            Text(adjunto.tipoDocumento, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${Fechas.paraMostrar(adjunto.fechaCarga)} · ${adjunto.cargadoPor}",
                style = MaterialTheme.typography.bodySmall
            )
            if (adjunto.comentario.isNotBlank()) Text(adjunto.comentario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// --------------------------- HISTORIAL (RF-06 / HU-04) ---------------------------
// ANIMACIÓN: AnimatedVisibility muestra / oculta el historial deslizándolo,
// y la flecha gira 180° con animateFloatAsState.
@Composable
fun SeccionHistorial(gestiones: List<Gestion>, mostrarVisibilidad: Boolean) {
    // rememberSaveable: si se gira el teléfono, se recuerda si estaba abierto
    var abierto by rememberSaveable { mutableStateOf(true) }
    val giro by animateFloatAsState(if (abierto) 180f else 0f, label = "flecha")

    TarjetaSeccion(titulo = stringResource(R.string.seccion_historial), modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { abierto = !abierto }
                .padding(vertical = Dimens.espacioMini),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(if (abierto) R.string.btn_ocultar_historial else R.string.btn_ver_historial, gestiones.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(painterResource(R.drawable.ic_flecha_abajo), contentDescription = null, modifier = Modifier.rotate(giro))
        }
        AnimatedVisibility(
            visible = abierto,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                gestiones.forEach { ItemHistorial(it, mostrarVisibilidad) }
            }
        }
    }
}

@Composable
private fun ItemHistorial(gestion: Gestion, mostrarVisibilidad: Boolean) {
    Row {
        // Puntito de "línea de tiempo"
        Box(
            Modifier
                .padding(top = 6.dp, end = Dimens.espacioChico)
                .size(10.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
        )
        Column {
            Text(
                "${Fechas.paraMostrar(gestion.fechaHora)} · ${gestion.tipo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(gestion.descripcion, style = MaterialTheme.typography.bodyLarge)
            Text(gestion.responsable, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            // Solo el liquidador ve cuáles son internas (el asegurado nunca las recibe, RN-13)
            if (mostrarVisibilidad && !gestion.visible) {
                Text(
                    stringResource(R.string.texto_gestion_interna),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
