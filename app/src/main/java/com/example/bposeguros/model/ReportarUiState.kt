package com.example.bposeguros.model

// Formulario "Reportar un siniestro" (RF-02 / HU-01).
// Las listas (compañías, tipos y comunas) vienen de la BD.
// Compañía y tipo se guardan como posición en la lista; la comuna, como su nombre.
data class ReportarUiState(
    val companias: List<Compania> = emptyList(),      // solo las ACTIVAS (RN-18)
    val tipos: List<TipoSiniestro> = emptyList(),     // filtrados según las líneas de la compañía
    val companiaElegida: Int? = null,
    val tipoElegido: Int? = null,
    val fechaOcurrencia: String = "",                 // como la escribe el usuario: dd-mm-aaaa
    val comuna: String = "",
    val comunas: List<String> = emptyList(),          // la lista viene de la BD (tabla comunas)
    val descripcion: String = "",

    val errorCompania: String? = null,
    val errorTipo: String? = null,
    val errorFecha: String? = null,
    val errorComuna: String? = null,
    val errorDescripcion: String? = null,

    val posibleDuplicado: String? = null,   // número del siniestro parecido (Aclaración 3)
    val numeroCreado: String? = null        // RF-03: número entregado al terminar
)
