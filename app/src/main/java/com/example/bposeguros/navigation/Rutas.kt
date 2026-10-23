package com.example.bposeguros.navigation

// Todas las rutas de la app estan en un solo lugar.
// Las que llevan {id} reciben un argumento; crear(id) arma la ruta completa.
sealed class Ruta(val ruta: String) {
    // "rut" es opcional: viene escrito cuando se llega desde el registro
    data object Login : Ruta("login?rut={rut}") {
        fun crear(rut: String = "") = "login?rut=$rut"
    }
    data object Registro : Ruta("registro")
    data object MisSiniestros : Ruta("mis_siniestros")
    data object Detalle : Ruta("detalle/{id}") { fun crear(id: Long) = "detalle/$id" }
    data object Reportar : Ruta("reportar")
    data object Adjuntar : Ruta("adjuntar/{id}") { fun crear(id: Long) = "adjuntar/$id" }
    data object Avisos : Ruta("avisos")

    data object Casos : Ruta("casos")
    data object Caso : Ruta("caso/{id}") { fun crear(id: Long) = "caso/$id" }
}



