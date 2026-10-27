package com.example.bposeguros.model

// Documentos que se pueden adjuntar o solicitar (sacados de la tabla de tipos de siniestro).
// Se dejan como lista fija porque no cambian: no hace falta una tabla para esto.
object TipoDocumento {
    val lista = listOf(
        "Fotografía del daño",
        "Constancia de denuncia policial",
        "Presupuesto de reparación",
        "Boletas o facturas",
        "Padrón del vehículo",
        "Informe o boleta del gasfíter",
        "Certificado de Bomberos",
        "Otro documento"
    )
}
