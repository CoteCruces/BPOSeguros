package com.example.bposeguros.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.permisos.Permisos
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.CampoSeleccion
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.AdjuntarViewModel
import kotlinx.coroutines.launch

// =====================================================================
//  AdjuntarScreen.kt  —  ADJUNTAR EVIDENCIA (RF-07 / HU-05)
//  RECURSOS NATIVOS: CÁMARA y GALERÍA, con los 4 pasos de permisos (igual que el
//  avatar del profesor). La pantalla consigue la imagen; el ViewModel la valida y guarda.
// =====================================================================
@Composable
fun AdjuntarScreen(
    idSiniestro: Long,
    idAsegurado: Long,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: AdjuntarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Se carga una vez al entrar (LaunchedEffect con la llave idSiniestro)
    LaunchedEffect(idSiniestro) { viewModel.cargar(idSiniestro, idAsegurado) }

    // Cuando el ViewModel avisa "guardado", se vuelve al detalle
    LaunchedEffect(uiState.guardado) { if (uiState.guardado) onGuardado() }

    // ---------- Permisos rechazados: Snackbar con botón "Ajustes" (paso 4) ----------
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamara = stringResource(R.string.permiso_camara_denegado)
    val textoGaleria = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)
    fun avisarDenegado(mensaje: String) {
        scope.launch {
            val r = snackbarHostState.showSnackbar(mensaje, textoAjustes, duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    // ---------- CÁMARA ----------
    // TakePicturePreview abre la cámara y devuelve un Bitmap (o null si el usuario canceló)
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto: Bitmap? ->
        if (foto != null) viewModel.onFotoTomada(foto)
    }
    fun abrirCamara() {
        try { lanzadorCamara.launch(null) } catch (e: ActivityNotFoundException) { }
    }
    val pedirPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) abrirCamara() else avisarDenegado(textoCamara)
    }

    // ---------- GALERÍA ----------
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) viewModel.onImagenElegida(uri)
    }
    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try { lanzadorGaleria.launch(intent) } catch (e: ActivityNotFoundException) { }
    }
    val pedirPermisoGaleria = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) abrirGaleria() else avisarDenegado(textoGaleria)
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_adjuntar), onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                stringResource(R.string.texto_adjuntar_a, uiState.numeroSiniestro),
                style = MaterialTheme.typography.titleMedium
            )

            // RN-10 / RN-11: si no se puede adjuntar, se explica por qué y no se muestra el formulario
            uiState.errorGeneral?.let { mensaje ->
                TarjetaSeccion(titulo = stringResource(R.string.titulo_no_se_puede)) {
                    Text(mensaje, style = MaterialTheme.typography.bodyLarge)
                }
                return@Column
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_que_documento)) {
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_tipo_documento),
                    opciones = uiState.opciones.map { it.texto },
                    seleccionada = uiState.opcionElegida,
                    onSeleccionar = viewModel::onOpcionElegida,
                    icono = R.drawable.ic_documento,
                    error = uiState.errorTipo
                )
                CampoTexto(
                    valor = uiState.comentario,
                    onValorCambia = viewModel::onComentarioCambia,
                    etiqueta = stringResource(R.string.campo_comentario),
                    icono = R.drawable.ic_editar,
                    ayuda = stringResource(R.string.ayuda_opcional),
                    lineas = 2
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_imagen)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonConIcono(R.drawable.ic_camara, stringResource(R.string.btn_tomar_foto), Modifier.weight(1f)) {
                        // PERMISOS paso 2: ¿ya está concedido? -> usar. ¿No? -> pedir (paso 3)
                        if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                        else pedirPermisoCamara.launch(Permisos.CAMARA)
                    }
                    BotonConIcono(R.drawable.ic_galeria, stringResource(R.string.btn_galeria), Modifier.weight(1f)) {
                        if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
                        else pedirPermisoGaleria.launch(Permisos.IMAGENES)
                    }
                }
                uiState.errorFoto?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                // ANIMACIÓN: la vista previa aparece creciendo y con fundido
                AnimatedVisibility(visible = uiState.foto != null, enter = scaleIn() + fadeIn()) {
                    uiState.foto?.let { foto ->
                        Image(
                            bitmap = foto.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_vista_previa),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(Dimens.radioCampo))
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_enviar_documento),
                onClick = viewModel::onGuardar,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BotonConIcono(icono: Int, texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(Dimens.alturaBoton)) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
        Spacer(Modifier.width(Dimens.espacioChico))
        Text(texto)
    }
}
