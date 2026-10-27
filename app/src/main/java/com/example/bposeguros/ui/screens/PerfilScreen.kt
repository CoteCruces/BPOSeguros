package com.example.bposeguros.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.permisos.Permisos
import com.example.bposeguros.ui.components.AvatarUsuario
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoSeleccion
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.DialogoCerrarSesion
import com.example.bposeguros.ui.components.FilaDato
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    onCambiarClave: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var preguntarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(usuario.id) { viewModel.cargar(usuario) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoGuardado = stringResource(R.string.texto_perfil_guardado)
    val textoCamara = stringResource(R.string.permiso_camara_denegado)
    val textoGaleria = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)
    fun avisarDenegado(mensaje: String) {
        scope.launch {
            val r = snackbarHostState.showSnackbar(mensaje, textoAjustes, duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            onGuardado()
            snackbarHostState.showSnackbar(textoGuardado)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto: Bitmap? ->
        if (foto != null) viewModel.onFotoTomada(foto)
    }
    fun abrirCamara() {
        try { lanzadorCamara.launch(null) } catch (e: ActivityNotFoundException) { }
    }
    val pedirPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) abrirCamara() else avisarDenegado(textoCamara)
    }

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
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_perfil), onVolver = onVolver) },
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
            AvatarUsuario(
                ruta = uiState.fotoActual,
                fotoNueva = uiState.fotoNueva,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                modifier = Modifier.fillMaxWidth()
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_tomar_foto),
                    onClick = {
                        if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                        else pedirPermisoCamara.launch(Permisos.CAMARA)
                    },
                    modifier = Modifier.weight(1f)
                )
                BotonSecundario(
                    texto = stringResource(R.string.btn_galeria),
                    onClick = {
                        if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
                        else pedirPermisoGaleria.launch(Permisos.IMAGENES)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.fotoActual.isNotBlank() || uiState.fotoNueva != null) {
                TextButton(onClick = viewModel::onQuitarFoto, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.btn_quitar_foto))
                }
            }
            uiState.errorFoto?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_cuenta)) {
                FilaDato(R.drawable.ic_rut, stringResource(R.string.texto_rut_fijo, usuario.rutFormateado()))
                if (!usuario.esAsegurado) {
                    FilaDato(R.drawable.ic_persona, stringResource(R.string.texto_liquidador_fijo, usuario.codigo, usuario.equipo))
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                CampoTexto(
                    valor = uiState.nombre,
                    onValorCambia = viewModel::onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre),
                    icono = R.drawable.ic_persona,
                    error = uiState.errorNombre
                )
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = viewModel::onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.telefono,
                    onValorCambia = viewModel::onTelefonoCambia,
                    etiqueta = stringResource(R.string.campo_telefono),
                    icono = R.drawable.ic_telefono,
                    error = uiState.errorTelefono,
                    tipoTeclado = KeyboardType.Phone
                )
                val opcionesComuna = listOf(stringResource(R.string.opcion_sin_indicar)) + uiState.comunas
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_comuna_perfil),
                    opciones = opcionesComuna,
                    seleccionada = if (uiState.comuna.isBlank()) null else uiState.comunas.indexOf(uiState.comuna) + 1,
                    onSeleccionar = { posicion ->
                        viewModel.onComunaCambia(if (posicion == 0) "" else uiState.comunas[posicion - 1])
                    },
                    icono = R.drawable.ic_ubicacion,
                    error = uiState.errorComuna
                )
                CampoTexto(
                    valor = uiState.fechaNacimiento,
                    onValorCambia = viewModel::onFechaCambia,
                    etiqueta = stringResource(R.string.campo_fecha_nacimiento),
                    icono = R.drawable.ic_calendario,
                    error = uiState.errorFecha,
                    ayuda = stringResource(R.string.ayuda_fecha_nacimiento),
                    tipoTeclado = KeyboardType.Number
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_cambios),
                onClick = { viewModel.onGuardar(usuario) },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
            BotonSecundario(
                texto = stringResource(R.string.btn_cambiar_clave),
                onClick = onCambiarClave,
                modifier = Modifier.fillMaxWidth()
            )
            BotonSecundario(
                texto = stringResource(R.string.btn_cerrar_sesion),
                onClick = { preguntarSalida = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (preguntarSalida) {
        DialogoCerrarSesion(onConfirmar = onCerrarSesion, onCancelar = { preguntarSalida = false })
    }
}
