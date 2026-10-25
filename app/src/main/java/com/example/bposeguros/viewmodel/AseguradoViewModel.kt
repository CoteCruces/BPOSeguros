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

class AseguradoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _siniestros = MutableStateFlow<List<Siniestro>>(emptyList())
    val siniestros: StateFlow<List<Siniestro>> = _siniestros.asStateFlow()

    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()

    fun cargar(idAsegurado: Long) {
        _siniestros.value = db.listarSiniestrosDeAsegurado(idAsegurado)
        _avisos.value = db.listarAvisos(idAsegurado)
    }

    fun cargarDetalle(idSiniestro: Long, idAsegurado: Long) {
        val siniestro = db.buscarSiniestroDeAsegurado(idSiniestro, idAsegurado)
        _detalle.value = if (siniestro == null) DetalleUiState() else DetalleUiState(
            siniestro = siniestro,
            gestiones = db.listarGestiones(idSiniestro, soloVisibles = true),
            solicitudes = db.listarSolicitudes(idSiniestro),
            adjuntos = db.listarAdjuntos(idSiniestro)
        )
    }

    fun marcarLeido(aviso: Aviso, idAsegurado: Long) {
        if (!aviso.leido) db.marcarAvisoLeido(aviso.id)
        _avisos.value = db.listarAvisos(idAsegurado)
    }
}
