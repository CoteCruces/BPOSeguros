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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bposeguros.R
import com.example.bposeguros.model.FormularioUiState
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.components.BotonSecundario
import com.example.bposeguros.ui.components.CampoContrasena
import com.example.bposeguros.ui.components.CampoTexto
import com.example.bposeguros.ui.components.CasillaConTexto
import com.example.bposeguros.ui.components.ImagenEncabezado
import com.example.bposeguros.ui.components.TarjetaSeccion
import com.example.bposeguros.ui.styles.EstilosTarjeta
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.Exito
import com.example.bposeguros.ui.theme.ExitoFondo
import com.example.bposeguros.viewmodel.FormularioViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.example.bposeguros.ui.styles.EstilosCampo

// =====================================================================
//  FormularioScreen.kt  —  PANTALLA DE INGRESO (la "V")
//   1) RegistroScreen  -> CON estado: conoce el ViewModel.
//   2) RegistroContent -> SIN estado: recibe datos y lambdas (se ve en @Preview).
//  La pantalla NO navega: solo AVISA "ingresé" con onRegistroExitoso(usuario).
//  AppNavegacion decide a dónde ir.

@Composable
fun FormularioScreen(
    onIrAIngresar: (String) -> Unit,
    //usuarioActual: Usuario?,                       //  quién inició sesión (o null)
    //onVerMiPerfil: () -> Unit,                     //  opción del menú ☰
    //onVerUsuarios: () -> Unit,                     //  opción del menú ☰
    //onCerrarSesion: () -> Unit,                    //  opción del menú ☰
    viewModel: FormularioViewModel = viewModel()   // se crea una vez y sobrevive a la rotación
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FormularioContent(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onComunaSeleccionada = viewModel::onTelefonoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        // ?.let = "si NO es null, ejecuta" -> solo navega si los datos son válidos
        onAceptaTerminos = viewModel::onAceptaTerminos,
        onIngresar = { viewModel.onIngresar() },  // solo valida; no navega
        onLimpiar = viewModel::onLimpiar,
        onIrAIngresar = { onIrAIngresar(uiState.rut) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onComunaSeleccionada: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onAceptaTerminos: (Boolean) -> Unit,
    onIngresar: () -> Unit,
    onLimpiar: () -> Unit,
    onIrAIngresar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_inicio)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
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
            ImagenEncabezado(
                imagen = R.drawable.banner_registro_bpo,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_titulo),
                subtitulo = stringResource(R.string.banner_subtitulo)
            )
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                //Datos personales (card 1)
                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                    CampoTexto(
                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = stringResource(R.string.campo_nombre),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )
                    CampoTexto(
                        valor = uiState.rut,
                        onValorCambia = onRutCambia,
                        etiqueta = stringResource(R.string.campo_rut),
                        icono = R.drawable.ic_rut,
                        error = uiState.errorRut
                    )
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoTexto(
                        valor = uiState.telefono,
                        onValorCambia = onTelefonoCambia,
                        etiqueta = stringResource(R.string.campo_telefono),
                        icono = R.drawable.ic_telefono,
                        error = uiState.errorTelefono,
                        tipoTeclado = KeyboardType.Phone
                    )
                    var expandido by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandido,
                        onExpandedChange = { expandido = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.comuna,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.campo_comuna_registro)) },
                            leadingIcon = {
                                Icon(painterResource(R.drawable.ic_ubicacion), contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            isError = uiState.errorComuna != null,
                            supportingText = { uiState.errorComuna?.let { Text(it) } },
                            shape = EstilosCampo.forma,
                            colors = EstilosCampo.colores(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandido,
                            onDismissRequest = { expandido = false }
                        ) {
                            uiState.comunas.forEach { comuna ->
                                DropdownMenuItem(
                                    text = { Text(comuna) },
                                    onClick = {
                                        onComunaSeleccionada(comuna)
                                        expandido = false
                                    }
                                )
                            }
                        }
                    }
                    CampoContrasena(
                        valor = uiState.contrasena,
                        onValorCambia = onContrasenaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.contrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorContrasena
                    )
                }

                //Terminos
                Column {
                    CasillaConTexto(
                        texto = stringResource(R.string.opcion_terminos),
                        marcado = uiState.aceptaTerminos,
                        onCambio = onAceptaTerminos
                    )
                    uiState.errorTerminos?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioMedio)
                        )
                    }
                }
                //Botones
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_ingresar),
                        onClick = onIngresar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Se muestra solo si registroExitoso (en Compose se muestra/oculta con un "if")
                if (uiState.registroExitoso) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = EstilosTarjeta.forma,
                        colors = CardDefaults.cardColors(containerColor = ExitoFondo)
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                        ) {
                            Text(
                                // Este segundo argumento reemplaza el %1$s del strings.xml
                                text = stringResource(
                                    R.string.registro_exitoso,
                                    uiState.nombre.trim()
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Exito
                            )
                            BotonPrincipal(
                                texto = stringResource(R.string.btn_ir_ingresar),
                                onClick = onIrAIngresar,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun RegistroPreview() {
    BPOSegurosTheme() {
        FormularioContent (
            uiState = FormularioUiState(
                nombre = "Ana1",
                errorNombre = "El nombre no puede tener números ni símbolos",
                rut = "15.842.371-R",
                errorRut = "Ingresa un rut válido",
                correo = "ana@",
                errorCorreo = "Ingresa un correo válido",
                telefono = "+569 1235rfre",
                errorTelefono = "Ingresa sólo números",
                contrasena = "sdfr",
                errorContrasena = "La contraseña debe tener al menos 8 caracteres ",
                errorTerminos = "Acepta los términos"
            ),
            onNombreCambia = {},
            onRutCambia = {},
            onCorreoCambia = {},
            onTelefonoCambia = {},
            onComunaSeleccionada = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onAceptaTerminos = {},
            onIngresar = {},
            onLimpiar = {},
            onIrAIngresar = {}
        )
    }

}