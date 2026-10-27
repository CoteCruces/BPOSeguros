package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.data.Correo
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoContrasena
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo
import com.example.bposeguros.viewmodel.FormularioViewModel
import kotlinx.coroutines.launch

// =====================================================================
//  FormularioScreen.kt  —  El LIQUIDADOR crea la cuenta de un cliente
//  Es la antigua pantalla de registro (FormularioScreen), movida desde el inicio
//  a la vista del liquidador. Pide solo RUT, correo y contraseña temporal.
//   1) FormularioScreen  -> CON estado: conoce el ViewModel.
//   2) FormularioContent -> SIN estado: recibe datos y lambdas (se ve en @Preview).
//  Al terminar ofrece "Enviar datos por correo": abre la app de correo del
//  teléfono con el RUT y la contraseña temporal ya escritos (intent implícito).
// =====================================================================
@Composable
fun FormularioScreen(
    onVolver: () -> Unit,
    viewModel: FormularioViewModel = viewModel()   // se crea una vez y sobrevive a la rotación
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val asunto = stringResource(R.string.correo_cuenta_asunto)
    val cuerpo = stringResource(
        R.string.correo_cuenta_cuerpo, uiState.rut, uiState.contrasena
    )
    val sinAppCorreo = stringResource(R.string.texto_sin_app_correo)

    FormularioContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onVolver = onVolver,
        onRutCambia = viewModel::onRutCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onSugerir = viewModel::onSugerirContrasena,
        onCrear = viewModel::onCrear,
        onLimpiar = viewModel::onLimpiar,
        onEnviarCorreo = {
            // Si el teléfono no tiene app de correo, se avisa en el Snackbar
            if (!Correo.abrir(context, uiState.correo, asunto, cuerpo)) {
                scope.launch { snackbarHostState.showSnackbar(sinAppCorreo) }
            }
        },
        cuerpoCorreo = cuerpo,
        asuntoCorreo = asunto
    )
}

@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    onRutCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onSugerir: () -> Unit,
    onCrear: () -> Unit,
    onLimpiar: () -> Unit,
    onEnviarCorreo: () -> Unit,
    cuerpoCorreo: String,
    asuntoCorreo: String
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_nuevo_cliente), onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro_bpo,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_nuevo_cliente),
                subtitulo = stringResource(R.string.banner_nuevo_cliente_sub)
            )
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                // Se muestra el formulario O el resultado (en Compose se muestra/oculta con un "if")
                if (!uiState.cuentaCreada) {
                    TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_obligatorios)) {
                        CampoTexto(
                            valor = uiState.rut,
                            onValorCambia = onRutCambia,
                            etiqueta = stringResource(R.string.campo_rut),
                            icono = R.drawable.ic_rut,
                            error = uiState.errorRut,
                            ayuda = stringResource(R.string.ayuda_rut)
                        )
                        CampoTexto(
                            valor = uiState.correo,
                            onValorCambia = onCorreoCambia,
                            etiqueta = stringResource(R.string.campo_correo),
                            icono = R.drawable.ic_correo,
                            error = uiState.errorCorreo,
                            tipoTeclado = KeyboardType.Email
                        )
                        CampoContrasena(
                            valor = uiState.contrasena,
                            onValorCambia = onContrasenaCambia,
                            etiqueta = stringResource(R.string.campo_clave_temporal),
                            visible = uiState.contrasenaVisible,
                            onAlternarVisible = onAlternarVisibilidad,
                            error = uiState.errorContrasena
                        )
                        TextButton(onClick = onSugerir, modifier = Modifier.align(Alignment.End)) {
                            Text(stringResource(R.string.btn_sugerir_clave))
                        }
                        Text(
                            stringResource(R.string.texto_resto_perfil),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    //Botones
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                        BotonSecundario(
                            texto = stringResource(R.string.btn_limpiar),
                            onClick = onLimpiar,
                            modifier = Modifier.weight(1f)
                        )
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_crear_cuenta),
                            onClick = onCrear,
                            icono = R.drawable.ic_check,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = EstilosTarjeta.forma,
                        colors = CardDefaults.cardColors(containerColor = ExitoFondo)
                    ) {
                        Text(
                            // Este segundo argumento reemplaza el %1$s del strings.xml
                            text = stringResource(R.string.cuenta_creada, uiState.rut),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Exito,
                            modifier = Modifier.padding(Dimens.espacioMedio)
                        )
                    }
                    // Vista previa de lo que recibirá el cliente
                    CorreoSimulado(
                        para = uiState.correo, asunto = asuntoCorreo, cuerpo = cuerpoCorreo,
                        titulo = stringResource(R.string.titulo_vista_previa_correo)
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_enviar_correo),
                        onClick = onEnviarCorreo,
                        icono = R.drawable.ic_correo,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_crear_otra),
                        onClick = onLimpiar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FormularioPreview() {
    BPOSegurosTheme {
        FormularioContent(
            uiState = FormularioUiState(
                rut = "15.842.371-R",
                errorRut = "El RUT no es válido. Revisa el dígito verificador",
                correo = "ana@",
                errorCorreo = "Ingresa un correo válido (ejemplo: usuario@dominio.com)",
                contrasena = "sdfr",
                errorContrasena = "Debe tener al menos 8 caracteres"
            ),
            snackbarHostState = SnackbarHostState(),
            onVolver = {},
            onRutCambia = {},
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onSugerir = {},
            onCrear = {},
            onLimpiar = {},
            onEnviarCorreo = {},
            cuerpoCorreo = "",
            asuntoCorreo = ""
        )
    }
}
