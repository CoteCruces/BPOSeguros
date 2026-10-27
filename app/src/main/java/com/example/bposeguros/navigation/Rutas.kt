package com.example.bposeguros.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("bienvenida")
    data object RecuperarClave : Ruta("recuperar_clave")
    data object CambiarClave : Ruta("cambiar_clave?obligatorio={obligatorio}") {
        fun crear(obligatorio: Boolean) = "cambiar_clave?obligatorio=$obligatorio"
    }
    data object Perfil : Ruta("perfil")

    data object MisSiniestros : Ruta("mis_siniestros")
    data object Resultados : Ruta("resultados?desde={desde}&hasta={hasta}") {
        fun crear(desde: String = "", hasta: String = "") = "resultados?desde=$desde&hasta=$hasta"
    }
    data object Detalle : Ruta("detalle/{id}") { fun crear(id: Long) = "detalle/$id" }
    data object Reportar : Ruta("reportar")
    data object Adjuntar : Ruta("adjuntar/{id}") { fun crear(id: Long) = "adjuntar/$id" }
    data object Avisos : Ruta("avisos")

    data object Casos : Ruta("casos")
    data object Caso : Ruta("caso/{id}") { fun crear(id: Long) = "caso/$id" }
    data object Registro : Ruta("registro")
}
