package com.example.bposeguros.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.screens.AdjuntarScreen
import com.example.bposeguros.ui.screens.AvisosScreen
import com.example.bposeguros.ui.screens.CambiarClaveScreen
import com.example.bposeguros.ui.screens.CasoLiquidadorScreen
import com.example.bposeguros.ui.screens.CasosLiquidadorScreen
import com.example.bposeguros.ui.screens.DetalleSiniestroScreen
import com.example.bposeguros.ui.screens.LoginScreen
import com.example.bposeguros.ui.screens.MisSiniestrosScreen
import com.example.bposeguros.ui.screens.FormularioScreen
import com.example.bposeguros.ui.screens.PerfilScreen
import com.example.bposeguros.ui.screens.RecuperarClaveScreen
import com.example.bposeguros.ui.screens.ReportarScreen
import com.example.bposeguros.ui.screens.ResultadosScreen
import com.example.bposeguros.viewmodel.AseguradoViewModel
import com.example.bposeguros.viewmodel.LiquidadorViewModel
import com.example.bposeguros.viewmodel.SesionViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    val sesionViewModel: SesionViewModel = viewModel()
    val aseguradoViewModel: AseguradoViewModel = viewModel()
    val liquidadorViewModel: LiquidadorViewModel = viewModel()

    val usuario by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()

    val inicio = remember { rutaInicial(sesionViewModel.usuarioActual.value) }

    val cerrarSesion = {
        sesionViewModel.cerrarSesion()
        liquidadorViewModel.limpiar()
        navController.navigate(Ruta.Login.ruta) { popUpTo(0) }
    }

    NavHost(
        navController = navController,
        startDestination = inicio,
        enterTransition = { slideInHorizontally { ancho -> ancho / 3 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { ancho -> ancho / 3 } + fadeOut() }
    ) {


        composable(Ruta.Login.ruta) {
            LoginScreen(
                onIngresoExitoso = { usuarioIngresado ->
                    sesionViewModel.iniciarSesion(usuarioIngresado)
                    navController.navigate(rutaInicial(usuarioIngresado)) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onOlvideClave = { navController.navigate(Ruta.RecuperarClave.ruta) }
            )
        }

        composable(Ruta.RecuperarClave.ruta) {
            RecuperarClaveScreen(onVolver = { navController.navigateUp() })
        }

        composable(
            route = Ruta.CambiarClave.ruta,
            arguments = listOf(navArgument("obligatorio") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { entrada ->
            val actual = usuario ?: return@composable
            val obligatorio = entrada.arguments?.getBoolean("obligatorio") ?: false
            CambiarClaveScreen(
                idUsuario = actual.id,
                obligatorio = obligatorio,
                onVolver = { if (obligatorio) cerrarSesion() else navController.navigateUp() },
                onCambiada = {
                    sesionViewModel.refrescar()
                    if (obligatorio) {
                        navController.navigate(rutaPrincipal(actual)) { popUpTo(0) }
                    } else {
                        navController.navigateUp()
                    }
                }
            )
        }

        composable(Ruta.Perfil.ruta) {
            val actual = usuario ?: return@composable
            PerfilScreen(
                usuario = actual,
                onVolver = { navController.navigateUp() },
                onGuardado = { sesionViewModel.refrescar() },
                onCambiarClave = { navController.navigate(Ruta.CambiarClave.crear(obligatorio = false)) },
                onCerrarSesion = cerrarSesion
            )
        }


        composable(Ruta.MisSiniestros.ruta) {
            val actual = usuario ?: return@composable
            val ultimos by aseguradoViewModel.ultimos.collectAsStateWithLifecycle()
            val avisos by aseguradoViewModel.avisos.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { aseguradoViewModel.cargar(actual.id) }

            MisSiniestrosScreen(
                usuario = actual,
                ultimos = ultimos,
                avisosSinLeer = avisos.count { !it.leido },
                onAbrir = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
                onReportar = { navController.navigate(Ruta.Reportar.ruta) },
                onVerAvisos = { navController.navigate(Ruta.Avisos.ruta) },
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onBuscarPorFechas = { desde, hasta -> navController.navigate(Ruta.Resultados.crear(desde, hasta)) },
                onVerTodos = { navController.navigate(Ruta.Resultados.crear()) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Ruta.Resultados.ruta,
            arguments = listOf(
                navArgument("desde") { type = NavType.StringType; defaultValue = "" },
                navArgument("hasta") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entrada ->
            val actual = usuario ?: return@composable
            val desde = entrada.arguments?.getString("desde").orEmpty()
            val hasta = entrada.arguments?.getString("hasta").orEmpty()
            val resultados by aseguradoViewModel.resultados.collectAsStateWithLifecycle()
            LaunchedEffect(desde, hasta) { aseguradoViewModel.buscar(actual.id, desde, hasta) }

            ResultadosScreen(
                desde = desde,
                hasta = hasta,
                siniestros = resultados,
                onVolver = { navController.navigateUp() },
                onAbrir = { id -> navController.navigate(Ruta.Detalle.crear(id)) }
            )
        }

        composable(Ruta.Detalle.ruta, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entrada ->
            val actual = usuario ?: return@composable
            val id = idDe(entrada)
            val detalle by aseguradoViewModel.detalle.collectAsStateWithLifecycle()
            LaunchedEffect(id) { aseguradoViewModel.cargarDetalle(id, actual.id) }

            DetalleSiniestroScreen(
                detalle = detalle,
                onVolver = { navController.navigateUp() },
                onAdjuntar = { navController.navigate(Ruta.Adjuntar.crear(id)) }
            )
        }

        composable(Ruta.Reportar.ruta) {
            val actual = usuario ?: return@composable
            ReportarScreen(
                idAsegurado = actual.id,
                onVolver = { navController.navigateUp() },
                onTerminar = { navController.navigateUp() }
            )
        }

        composable(Ruta.Adjuntar.ruta, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entrada ->
            val actual = usuario ?: return@composable
            val id = idDe(entrada)
            AdjuntarScreen(
                idSiniestro = id,
                idAsegurado = actual.id,
                onVolver = { navController.navigateUp() },
                onGuardado = {
                    aseguradoViewModel.cargarDetalle(id, actual.id)
                    navController.navigateUp()
                }
            )
        }

        composable(Ruta.Avisos.ruta) {
            val actual = usuario ?: return@composable
            val avisos by aseguradoViewModel.avisos.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { aseguradoViewModel.cargar(actual.id) }

            AvisosScreen(
                avisos = avisos,
                onVolver = { navController.navigateUp() },
                onAbrir = { aviso ->
                    aseguradoViewModel.marcarLeido(aviso, actual.id)
                    navController.navigate(Ruta.Detalle.crear(aviso.siniestroId))
                }
            )
        }


        composable(Ruta.Casos.ruta) {
            val actual = usuario ?: return@composable
            val casos by liquidadorViewModel.casos.collectAsStateWithLifecycle()
            val busqueda by liquidadorViewModel.busqueda.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { liquidadorViewModel.cargarCasos(actual.id) }

            CasosLiquidadorScreen(
                usuario = actual,
                casos = casos,
                busqueda = busqueda,
                onBusquedaCambia = { texto -> liquidadorViewModel.onBusquedaCambia(texto, actual.id) },
                onAbrir = { id -> navController.navigate(Ruta.Caso.crear(id)) },
                onNuevoCliente = { navController.navigate(Ruta.Registro.ruta) },
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Ruta.Caso.ruta, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entrada ->
            val actual = usuario ?: return@composable
            CasoLiquidadorScreen(
                idSiniestro = idDe(entrada),
                liquidador = actual,
                viewModel = liquidadorViewModel,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.Registro.ruta) {
            FormularioScreen(onVolver = { navController.navigateUp() })
        }
    }
}

private fun rutaInicial(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta
    usuario.claveTemporal -> Ruta.CambiarClave.crear(obligatorio = true)
    else -> rutaPrincipal(usuario)
}

private fun rutaPrincipal(usuario: Usuario): String =
    if (usuario.esAsegurado) Ruta.MisSiniestros.ruta else Ruta.Casos.ruta

private fun idDe(entrada: NavBackStackEntry): Long = entrada.arguments?.getLong("id") ?: 0L
