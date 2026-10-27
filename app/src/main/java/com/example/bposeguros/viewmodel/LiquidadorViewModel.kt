package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.Notificador
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.AccionesUiState
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.DialogoLiquidador
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento
import com.example.bposeguros.model.TipoDocumento
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LiquidadorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val _casos = MutableStateFlow<List<Siniestro>>(emptyList())
    val casos: StateFlow<List<Siniestro>> = _casos.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()

    private val _acciones = MutableStateFlow(AccionesUiState())
    val acciones: StateFlow<AccionesUiState> = _acciones.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()



    fun cargarCasos(idLiquidador: Long) {
        val texto = _busqueda.value.trim()
        _casos.value =
            if (texto.isEmpty()) db.listarSiniestrosAsignados(idLiquidador)
            else db.buscarCasos(idLiquidador, texto, texto.replace(".", "").replace("-", "").uppercase())
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
        val siniestro = db.buscarSiniestroDeLiquidador(idSiniestro, idLiquidador)
        _detalle.value = if (siniestro == null) DetalleUiState() else DetalleUiState(
            siniestro = siniestro,
            gestiones = db.listarGestiones(idSiniestro, soloVisibles = false),
            solicitudes = db.listarSolicitudes(idSiniestro),
            adjuntos = db.listarAdjuntos(idSiniestro)
        )
    }



    fun abrirDialogo(dialogo: DialogoLiquidador) {
        _acciones.value = AccionesUiState(dialogo = dialogo)
    }

    fun cerrarDialogo() {
        _acciones.update { it.copy(dialogo = null) }
    }

    fun onDescripcionGestion(valor: String) =
        _acciones.update { it.copy(descripcionGestion = valor.take(300), errorGestion = null) }

    fun onGestionVisible(valor: Boolean) = _acciones.update { it.copy(gestionVisible = valor) }

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
        db.insertarGestion(
            Gestion(
                siniestroId = siniestro.id, fechaHora = Fechas.ahora(), tipo = "Gestión del liquidador",
                descripcion = estado.descripcionGestion.trim(), responsable = liquidador.comoResponsable(),
                visible = estado.gestionVisible
            )
        )
        terminar(siniestro, liquidador, "Gestión registrada")
    }

    fun solicitarDocumento(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val estado = _acciones.value
        if (estado.documentoElegido == null) {
            _acciones.update { it.copy(errorDocumento = "Elige qué documento necesitas") }
            return
        }
        val documento = TipoDocumento.lista[estado.documentoElegido]
        val ahora = Fechas.ahora()
        db.insertarSolicitud(
            SolicitudDocumento(
                siniestroId = siniestro.id, tipoDocumento = documento,
                comentario = estado.comentarioSolicitud.trim(), fechaSolicitud = ahora.take(10)
            )
        )
        val comentario = estado.comentarioSolicitud.trim()
        val texto = "Necesitamos: ${documento.lowercase()}." + if (comentario.isEmpty()) "" else " $comentario"
        db.insertarGestion(
            Gestion(
                siniestroId = siniestro.id, fechaHora = ahora, tipo = "Solicitud de documento",
                descripcion = texto, responsable = liquidador.comoResponsable(), visible = true
            )
        )
        avisar(siniestro, ahora, "Solicitud de documento",
            "Necesitamos ${documento.lowercase()} para tu siniestro ${siniestro.numero}.")
        terminar(siniestro, liquidador, "Documento solicitado")
    }

    fun avanzarEtapa(liquidador: Usuario) {
        val siniestro = _detalle.value.siniestro ?: return
        val siguiente = siniestro.etapa.siguiente()
        if (siguiente == null) {
            _acciones.update { it.copy(mensaje = "Este caso no se puede avanzar desde su etapa actual") }
            return
        }
        if (db.contarPendientes(siniestro.id) > 0) {
            _acciones.update { it.copy(mensaje = "Primero deben estar entregados todos los documentos pendientes") }
            return
        }
        val ahora = Fechas.ahora()
        db.actualizarEtapa(siniestro.id, siguiente, ahora.take(10))
        val nombreEtapa = getApplication<Application>().getString(siguiente.etiqueta)
        db.insertarGestion(
            Gestion(
                siniestroId = siniestro.id, fechaHora = ahora, tipo = "Cambio de etapa",
                descripcion = "Tu caso pasó a \"$nombreEtapa\". Estamos definiendo la resolución con tu compañía.",
                responsable = liquidador.comoResponsable(), visible = true
            )
        )
        avisar(siniestro, ahora, "Cambio de etapa", "Tu siniestro ${siniestro.numero} pasó a \"$nombreEtapa\".")
        terminar(siniestro, liquidador, "El caso avanzó a $nombreEtapa")
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
        val ahora = Fechas.ahora()

        db.cerrarSiniestro(siniestro.id, resultado, estado.mensajeCierre.trim(), ahora.take(10))
        db.insertarGestion(
            Gestion(
                siniestroId = siniestro.id, fechaHora = ahora, tipo = "Cierre",
                descripcion = "Tu caso fue cerrado. Resultado: $nombreResultado.",
                responsable = liquidador.comoResponsable(), visible = true
            )
        )
        avisar(siniestro, ahora, "Cierre",
            "Tu siniestro ${siniestro.numero} fue cerrado ($nombreResultado). Revisa el mensaje de cierre.")
        terminar(siniestro, liquidador, "Caso cerrado")
    }


    private fun avisar(siniestro: Siniestro, ahora: String, motivo: String, mensaje: String) {
        db.insertarAviso(siniestro.id, ahora, motivo, mensaje)
        Notificador.mostrar(getApplication(), siniestro.id, motivo, mensaje)
    }

    private fun terminar(siniestro: Siniestro, liquidador: Usuario, mensaje: String) {
        _acciones.value = AccionesUiState(mensaje = mensaje)
        cargarDetalle(siniestro.id, liquidador.id)
        cargarCasos(liquidador.id)
    }
}
