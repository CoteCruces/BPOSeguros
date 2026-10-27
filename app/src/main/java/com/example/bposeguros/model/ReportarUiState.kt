package com.example.bposeguros.model

data class ReportarUiState(
    val companias: List<Compania> = emptyList(),
    val tipos: List<TipoSiniestro> = emptyList(),
    val companiaElegida: Int? = null,
    val tipoElegido: Int? = null,
    val fechaOcurrencia: String = "",
    val comuna: String = "",
    val comunas: List<String> = emptyList(),
    val descripcion: String = "",

    val errorCompania: String? = null,
    val errorTipo: String? = null,
    val errorFecha: String? = null,
    val errorComuna: String? = null,
    val errorDescripcion: String? = null,

    val posibleDuplicado: String? = null,
    val numeroCreado: String? = null
)
