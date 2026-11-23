package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.Notificador
import com.example.bposeguros.data.SiniestroRepository
import com.example.bposeguros.data.avisoCorto
import com.example.bposeguros.model.AccionesUiState
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.DialogoLiquidador
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.TipoDocumento
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiquidadorViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = SiniestroRepository()

    private val _casos = MutableStateFlow<List<Siniestro>>(emptyList())
    val casos: StateFlow<List<Siniestro>> = _casos.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()

    private val _acciones = MutableStateFlow(AccionesUiState())
    val acciones: StateFlow<AccionesUiState> = _acciones.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()


    private var cargaDeCasos: Job? = null

    fun cargarCasos(idLiquidador: Long) {
        cargaDeCasos?.cancel()
        cargaDeCasos = viewModelScope.launch { recargarCasos(idLiquidador) }
    }

    private suspend fun recargarCasos(idLiquidador: Long) {
        val texto = _busqueda.value.trim()
        val resultado =
            if (texto.isEmpty()) repositorio.siniestrosAsignados(idLiquidador)
            else repositorio.buscarCasos(idLiquidador, texto)
        resultado
            .onSuccess { _casos.value = it }
            .onFailure { getApplication<Application>().avisoCorto(it.message.orEmpty()) }
    }

    fun onBusquedaCambia(texto: String, idLiquidador: Long) {
        _busqueda.value = texto
        cargarCasos(idLiquidador)
    }

    fun limpiar() {
        _busqueda.value = ""
        _casos.value = emptyList()
    }

    fun cargarDetalle(idSiniestro: Long, idLiquidador: Long) {
        viewModelScope.launch { recargarDetalle(idSiniestro, idLiquidador) }
    }

    private suspend fun recargarDetalle(idSiniestro: Long, idLiquidador: Long) {

        repositorio.detalle(idSiniestro, aseguradoId = null, liquidadorId = idLiquidador, soloVisibles = false)
            .onSuccess { _detalle.value = it }
            .onFailure { getApplication<Application>().avisoCorto(it.message.orEmpty()) }
    }

    fun abrirDialogo(dialogo: DialogoLiquidador) {
        _acciones.value = AccionesUiState(dialogo = dialogo)
    }

    fun cerrarDialogo() {
        _acciones.update { it.copy(dialogo = null) }
    }

    fun onDescripcionGestion(valor: String) =
        _acciones.update { it.copy(descripcionGestion = valor.take(300), errorGestion = null) }

    fun onDocumentoElegido(posicion: Int) =
        _acciones.update { it.copy(documentoElegido = posicion, errorDocumento = null) }

    fun onComentarioSolicitud(valor: String) =
        _acciones.update { it.copy(comentarioSolicitud = valor.take(300)) }

    fun onResultadoElegido(posicion: Int) =
        _acciones.update { it.copy(resultadoElegido = posicion, errorResultado = null) }

    fun onMensajeCierre(valor: String) =
        _acciones.update { it.copy(mensajeCierre = valor.take(500), errorMensaje = null) }

    fun mensajeMostrado() = _acciones.update { it.copy(mensaje = null) }


    fun registrarGestion(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val estado = _acciones.value
        val error = when {
            estado.descripcionGestion.isBlank() -> "Describe lo que hiciste"
            estado.descripcionGestion.trim().length < 10 -> "Escribe un poco más (mínimo 10 caracteres)"
            else -> null
        }
        if (error != null) {
            _acciones.update { it.copy(errorGestion = error) }
            return
        }
        viewModelScope.launch {
            // El servidor la guarda como nota interna: el asegurado no la ve
            repositorio.registrarGestion(siniestro.id, liquidador.id, estado.descripcionGestion.trim())
                .onSuccess { terminar(siniestro, liquidador, "Gestión registrada") }
                .onFailure { e -> _acciones.update { it.copy(errorGestion = e.message) } }
        }
    }

    fun solicitarDocumento(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val estado = _acciones.value
        if (estado.documentoElegido == null) {
            _acciones.update { it.copy(errorDocumento = "Elige qué documento necesitas") }
            return
        }
        val documento = TipoDocumento.lista[estado.documentoElegido]
        viewModelScope.launch {
            repositorio.solicitarDocumento(siniestro.id, liquidador.id, documento, estado.comentarioSolicitud.trim())
                .onSuccess {
                    avisar(
                        siniestro, "Solicitud de documento",
                        "Necesitamos ${documento.lowercase()} para tu siniestro ${siniestro.numero}."
                    )
                    terminar(siniestro, liquidador, "Documento solicitado")
                }
                .onFailure { e -> _acciones.update { it.copy(errorDocumento = e.message) } }
        }
    }

    fun avanzarEtapa(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val siguiente = siniestro.etapa.siguiente()
        if (siguiente == null) {
            _acciones.update { it.copy(mensaje = "Este caso no se puede avanzar desde su etapa actual") }
            return
        }
        viewModelScope.launch {
            repositorio.avanzarEtapa(siniestro.id, liquidador.id)
                .onSuccess {
                    val nombreEtapa = getApplication<Application>().getString(siguiente.etiqueta)
                    avisar(siniestro, "Cambio de etapa", "Tu siniestro ${siniestro.numero} pasó a \"$nombreEtapa\".")
                    terminar(siniestro, liquidador, "El caso avanzó a $nombreEtapa")
                }

                .onFailure { e -> _acciones.update { it.copy(mensaje = e.message) } }
        }
    }


    fun validarCierre(): Boolean {
        val estado = _acciones.value
        val errorResultado = if (estado.resultadoElegido == null) "Elige el resultado" else null
        val errorMensaje = when {
            estado.mensajeCierre.isBlank() -> "Escribe un mensaje para el asegurado"
            estado.mensajeCierre.trim().length < 15 -> "Explica un poco más (mínimo 15 caracteres)"
            else -> null
        }
        _acciones.update { it.copy(errorResultado = errorResultado, errorMensaje = errorMensaje) }
        return errorResultado == null && errorMensaje == null
    }

    fun cerrarCaso(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val estado = _acciones.value
        val permitidos = siniestro.etapa.resultadosPermitidos()

        val errorResultado = if (estado.resultadoElegido == null) "Elige el resultado" else null
        val errorMensaje = when {
            estado.mensajeCierre.isBlank() -> "Escribe un mensaje para el asegurado"
            estado.mensajeCierre.trim().length < 15 -> "Explica un poco más (mínimo 15 caracteres)"
            else -> null
        }
        if (errorResultado != null || errorMensaje != null) {
            _acciones.update { it.copy(errorResultado = errorResultado, errorMensaje = errorMensaje) }
            return
        }
        val resultado = permitidos[estado.resultadoElegido!!]
        val nombreResultado = getApplication<Application>().getString(resultado.etiqueta)

        viewModelScope.launch {
            repositorio.cerrar(siniestro.id, liquidador.id, resultado.name, estado.mensajeCierre.trim())
                .onSuccess {
                    avisar(
                        siniestro, "Cierre",
                        "Tu siniestro ${siniestro.numero} fue cerrado ($nombreResultado). Revisa el mensaje de cierre."
                    )
                    terminar(siniestro, liquidador, "Caso cerrado")
                }
                .onFailure { e -> _acciones.update { it.copy(errorMensaje = e.message) } }
        }
    }



    private fun avisar(siniestro: Siniestro, motivo: String, mensaje: String) {
        Notificador.mostrar(getApplication<Application>(), siniestro.id, motivo, mensaje)
    }

    private suspend fun terminar(siniestro: Siniestro, liquidador: Usuario, mensaje: String) {
        _acciones.value = AccionesUiState(mensaje = mensaje)
        recargarDetalle(siniestro.id, liquidador.id)
        recargarCasos(liquidador.id)
    }
}