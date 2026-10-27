package com.example.bposeguros.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.AdjuntarUiState
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.OpcionDocumento
import com.example.bposeguros.model.TipoDocumento
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

class AdjuntarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)
    private var idSiniestro = 0L

    private val _uiState = MutableStateFlow(AdjuntarUiState())
    val uiState: StateFlow<AdjuntarUiState> = _uiState.asStateFlow()

    fun cargar(idSiniestro: Long, idAsegurado: Long) {
        this.idSiniestro = idSiniestro
        val siniestro = db.buscarSiniestroDeAsegurado(idSiniestro, idAsegurado)

        val pendientes = db.listarSolicitudes(idSiniestro).filter { !it.entregado }
        val pedidos = pendientes.map {
            OpcionDocumento(texto = "Documento pedido: ${it.tipoDocumento}", tipoDocumento = it.tipoDocumento, solicitudId = it.id)
        }

        val comunes = TipoDocumento.lista
            .filter { tipo -> pendientes.none { mismoDocumento(it.tipoDocumento, tipo) } }
            .map { OpcionDocumento(texto = it, tipoDocumento = it) }

        val errorGeneral = when {
            siniestro == null -> "No encontramos este siniestro"
            siniestro.estaCerrado -> "Tu caso está cerrado, por eso ya no se pueden adjuntar documentos"
            db.contarAdjuntos(idSiniestro) >= 15 -> "Este caso ya tiene el máximo de 15 documentos"
            else -> null
        }

        _uiState.value = AdjuntarUiState(
            numeroSiniestro = siniestro?.numero ?: "",
            opciones = pedidos + comunes,
            opcionElegida = if (pedidos.isNotEmpty()) 0 else null,
            errorGeneral = errorGeneral
        )
    }

    fun onOpcionElegida(posicion: Int) {
        _uiState.update { it.copy(opcionElegida = posicion, errorTipo = null) }
    }

    fun onComentarioCambia(valor: String) {
        _uiState.update { it.copy(comentario = valor.take(300)) }
    }

    fun onFotoTomada(foto: Bitmap) {
        _uiState.update { it.copy(foto = foto, errorFoto = null) }
    }

    fun onImagenElegida(uri: Uri) {
        val foto = ConvertidorImagen.desdeUri(getApplication(), uri)
        _uiState.update {
            if (foto == null) it.copy(errorFoto = "No pudimos abrir esa imagen. Prueba con otra")
            else it.copy(foto = foto, errorFoto = null)
        }
    }

    fun onGuardar() {
        val estado = _uiState.value
        if (estado.errorGeneral != null) return

        val errorTipo = if (estado.opcionElegida == null) "Indica qué documento es" else null
        val errorFoto = if (estado.foto == null) "Toma una foto o elige una imagen" else null
        _uiState.update { it.copy(errorTipo = errorTipo, errorFoto = errorFoto) }
        if (errorTipo != null || estado.foto == null) return

        val opcion = estado.opciones[estado.opcionElegida!!]
        val nombre = "adjunto_${estado.numeroSiniestro}_${System.currentTimeMillis()}.jpg"
        val archivo = ConvertidorImagen.guardar(getApplication(), estado.foto, nombre)

        if (archivo == null || archivo.length() / 1024 > ConvertidorImagen.MAXIMO_KB) {
            archivo?.delete()
            _uiState.update { it.copy(errorFoto = "La imagen es muy pesada (máximo 5 MB)") }
            return
        }

        val ahora = Fechas.ahora()

        val solicitudId = opcion.solicitudId ?: db.listarSolicitudes(idSiniestro)
            .firstOrNull { !it.entregado && mismoDocumento(it.tipoDocumento, opcion.tipoDocumento) }?.id

        db.insertarAdjunto(
            DocumentoAdjunto(
                siniestroId = idSiniestro,
                nombreArchivo = nombre,
                rutaArchivo = archivo.absolutePath,
                tipoDocumento = opcion.tipoDocumento,
                fechaCarga = ahora,
                cargadoPor = "Asegurado",
                comentario = estado.comentario.trim(),
                solicitudId = solicitudId
            )
        )
        if (solicitudId != null) db.marcarSolicitudEntregada(solicitudId, ahora)

        db.insertarGestion(
            Gestion(
                siniestroId = idSiniestro, fechaHora = ahora, tipo = "Documento recibido",
                descripcion = "Recibimos tu documento: ${opcion.tipoDocumento.lowercase()}.",
                responsable = "Asegurado", visible = true
            )
        )
        _uiState.update { it.copy(guardado = true) }
    }

    private fun mismoDocumento(pedido: String, enviado: String): Boolean {
        val a = sinTildes(pedido)
        val b = sinTildes(enviado)
        return a == b || a.startsWith(b) || b.startsWith(a)
    }

    private fun sinTildes(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}
