package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.Siniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// =====================================================================
//  AseguradoViewModel.kt  —  Datos que ve la persona asegurada
//  Se crea UNA vez en AppNavegacion y lo comparten: Mis siniestros, Resultados, Detalle y Avisos.
//  Todas las consultas reciben el id del asegurado (RNF-07: nunca ve casos de otra persona).
// =====================================================================
class AseguradoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    // Pantalla principal: SOLO los 2 últimos registrados (sugerencia del profesor)
    private val _ultimos = MutableStateFlow<List<Siniestro>>(emptyList())
    val ultimos: StateFlow<List<Siniestro>> = _ultimos.asStateFlow()

    // Pantalla "Resultados": los de un rango de fechas o todos
    private val _resultados = MutableStateFlow<List<Siniestro>>(emptyList())
    val resultados: StateFlow<List<Siniestro>> = _resultados.asStateFlow()

    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()

    // Se llama cada vez que se entra a "Mis siniestros" (así se ve lo recién reportado)
    fun cargar(idAsegurado: Long) {
        _ultimos.value = db.listarUltimosSiniestros(idAsegurado, cantidad = 2)
        _avisos.value = db.listarAvisos(idAsegurado)
    }

    // desde y hasta = "aaaa-mm-dd" -> los que ocurrieron entre esas fechas.  Vacías -> todos.
    fun buscar(idAsegurado: Long, desde: String, hasta: String) {
        _resultados.value =
            if (desde.isBlank() || hasta.isBlank()) db.listarSiniestrosDeAsegurado(idAsegurado)
            else db.listarSiniestrosEntreFechas(idAsegurado, desde, hasta)
    }

    fun cargarDetalle(idSiniestro: Long, idAsegurado: Long) {
        val siniestro = db.buscarSiniestroDeAsegurado(idSiniestro, idAsegurado)
        _detalle.value = if (siniestro == null) DetalleUiState() else DetalleUiState(
            siniestro = siniestro,
            gestiones = db.listarGestiones(idSiniestro, soloVisibles = true),   // RN-13
            solicitudes = db.listarSolicitudes(idSiniestro),
            adjuntos = db.listarAdjuntos(idSiniestro)
        )
    }

    fun marcarLeido(aviso: Aviso, idAsegurado: Long) {
        if (!aviso.leido) db.marcarAvisoLeido(aviso.id)
        _avisos.value = db.listarAvisos(idAsegurado)
    }
}
