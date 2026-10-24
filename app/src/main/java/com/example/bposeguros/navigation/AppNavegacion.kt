package com.example.bposeguros.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.ui.components.BotonPrincipal
import com.example.bposeguros.ui.screens.FormularioScreen
import com.example.bposeguros.ui.screens.LoginScreen
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.viewmodel.SesionViewModel
import com.example.bposeguros.ui.screens.MisSiniestrosScreen
import com.example.bposeguros.ui.screens.ReportarScreen
import com.example.bposeguros.viewmodel.AseguradoViewModel

// =====================================================================
//  AppNavegacion.kt — el ÚNICO lugar que decide a dónde se va.
//  Las pantallas no navegan solas: solo avisan "pasó esto" (onIngresoExitoso...)
//  y aquí se decide el destino.
// =====================================================================
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val sesionViewModel: SesionViewModel = viewModel()
    val usuarioActual by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Si ya hay una sesión guardada (SharedPreferences) se entra directo, sin pasar por el login.
    // remember { } sin claves: se calcula solo al abrir la app.
    val destinoInicial = remember {
        val usuario = usuarioActual
        when {
            usuario == null -> Ruta.Login.ruta
            usuario.rolId == Rol.ID_ASEGURADO -> Ruta.MisSiniestros.ruta
            else -> Ruta.Casos.ruta
        }
    }

    // Cierra la sesión y vuelve al login borrando todo el historial de pantallas
    val cerrarSesion = {
        sesionViewModel.cerrarSesion()
        navController.navigate(Ruta.Login.crear()) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable(
            route = Ruta.Login.ruta,
            arguments = listOf(navArgument("rut") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entrada ->
            LoginScreen(
                rutInicial = entrada.arguments?.getString("rut").orEmpty(),
                onIngresoExitoso = { usuario ->
                    sesionViewModel.iniciarSesion(usuario)          // guarda el id en SharedPreferences
                    val destino = if (usuario.rolId == Rol.ID_ASEGURADO)
                        Ruta.MisSiniestros.ruta else Ruta.Casos.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }  // "atrás" no vuelve al login
                    }
                },
                onIrARegistro = { navController.navigate(Ruta.Registro.ruta) }
            )
        }

        composable(Ruta.Registro.ruta) {
            FormularioScreen(
                onIrAIngresar = { rut ->
                    // El RUT viaja sin puntos ni guion; el login lo recibe escrito
                    val rutLimpio = rut.filter { it.isLetterOrDigit() }
                    navController.navigate(Ruta.Login.crear(rutLimpio)) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // ----- Pantallas pendientes: cada una se reemplaza en su propia rama -----
        composable(Ruta.MisSiniestros.ruta) {
            // El ViewModel que lee los siniestros y los avisos desde la BD
            val aseguradoViewModel: AseguradoViewModel = viewModel()
            val siniestros by aseguradoViewModel.siniestros.collectAsStateWithLifecycle()
            val avisos by aseguradoViewModel.avisos.collectAsStateWithLifecycle()

            // ?.let: si la sesión se cerró (usuario null) no se dibuja nada mientras se vuelve al login
            usuarioActual?.let { usuario ->
                // Se recarga cada vez que la pantalla aparece (por ejemplo al volver de "Reportar")
                LaunchedEffect(usuario.id) { aseguradoViewModel.cargar(usuario.id) }
                MisSiniestrosScreen(
                    usuario = usuario,
                    siniestros = siniestros,
                    avisosSinLeer = avisos.count{!it.leido},  // cuenta cuántos avisos no se han leído
                    onAbrir = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
                    onReportar = { navController.navigate(Ruta.Reportar.ruta) },
                    onVerAvisos = { navController.navigate(Ruta.Avisos.ruta) },
                    onCerrarSesion = cerrarSesion
                )
            }
        }


        composable(Ruta.Detalle.ruta) { Proximamente("Detalle del siniestro") }
        composable(Ruta.Reportar.ruta) {
            usuarioActual?.let { actual ->
                ReportarScreen(
                    idAsegurado = actual.id,
                    onVolver = { navController.navigateUp() },
                    onTerminar = { navController.navigateUp() }
                )
            }
        }
        composable(Ruta.Adjuntar.ruta) { Proximamente("Adjuntar documento") }
        composable(Ruta.Avisos.ruta) { Proximamente("Avisos") }
        composable(Ruta.Casos.ruta) { Proximamente("Casos del liquidador", cerrarSesion) }
        composable(Ruta.Caso.ruta) { Proximamente("Caso del liquidador") }
    }
}

// Pantalla temporal para poder probar la navegación. Se borra al terminar el proyecto.
@Composable
private fun Proximamente(titulo: String, onCerrarSesion: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.espacioPantalla),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleLarge)
        Text(text = "Próximamente", style = MaterialTheme.typography.bodyMedium)
        if (onCerrarSesion != null) {
            BotonPrincipal(texto = "Cerrar sesión", onClick = onCerrarSesion)
        }
    }
}