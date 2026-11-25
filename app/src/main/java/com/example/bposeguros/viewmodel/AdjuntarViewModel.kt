package com.example.bposeguros.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.data.SiniestroRepository
import com.example.bposeguros.model.AdjuntarUiState
import com.example.bposeguros.model.ArchivoElegido
import com.example.bposeguros.model.OpcionDocumento
import com.example.bposeguros.model.TipoDocumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.Normalizer

class AdjuntarViewModel(application: Application) : AndroidViewModel(application) {

    companion object {

        private const val MAXIMO_BYTES = 9 * 1024 * 1024
        const val MENSAJE_IMAGEN_CARGADA = "Imagen cargada con éxito"
    }

    private class ArchivoParaSubir(val bytes: ByteArray, val nombre: String, val tipo: String)

    private val repositorio = SiniestroRepository()
    private var idSiniestro = 0L
    private var guardando = false

    private val _uiState = MutableStateFlow(AdjuntarUiState())
    val uiState: StateFlow<AdjuntarUiState> = _uiState.asStateFlow()

    fun cargar(idSiniestro: Long, idAsegurado: Long) {
        this.idSiniestro = idSiniestro
        viewModelScope.launch {
            val resultado = repositorio.siniestroDeAsegurado(idSiniestro, idAsegurado)
            val siniestro = resultado.getOrNull()

            val pendientes = repositorio.solicitudes(idSiniestro).getOrDefault(emptyList()).filter { !it.entregado }
            val cantidadAdjuntos = repositorio.adjuntos(idSiniestro).getOrDefault(emptyList()).size

            val pedidos = pendientes.map {
                OpcionDocumento(
                    texto = "Documento pedido: ${it.tipoDocumento}",
                    tipoDocumento = it.tipoDocumento,
                    solicitudId = it.id
                )
            }

            val comunes = TipoDocumento.lista
                .filter { tipo -> pendientes.none { mismoDocumento(it.tipoDocumento, tipo) } }
                .map { OpcionDocumento(texto = it, tipoDocumento = it) }

            val errorGeneral = when {
                siniestro == null -> resultado.exceptionOrNull()?.message ?: "No encontramos este siniestro"
                siniestro.estaCerrado -> "Tu caso está cerrado, por eso ya no se pueden adjuntar documentos"
                cantidadAdjuntos >= 15 -> "Este caso ya tiene el máximo de 15 documentos"
                else -> null
            }

            _uiState.value = AdjuntarUiState(
                numeroSiniestro = siniestro?.numero ?: "",
                opciones = pedidos + comunes,
                opcionElegida = if (pedidos.isNotEmpty()) 0 else null,
                errorGeneral = errorGeneral
            )
        }
    }

    fun onOpcionElegida(posicion: Int) {
        _uiState.update { it.copy(opcionElegida = posicion, errorTipo = null) }
    }

    fun onComentarioCambia(valor: String) {
        _uiState.update { it.copy(comentario = valor.take(300)) }
    }


    fun onFotoTomada(foto: Bitmap) {
        _uiState.update {
            it.copy(foto = foto, documento = null, errorFoto = null, mensaje = MENSAJE_IMAGEN_CARGADA)
        }
    }

    fun onImagenElegida(uri: Uri) {
        val foto = ConvertidorImagen.desdeUri(getApplication(), uri)
        _uiState.update {
            if (foto == null) it.copy(errorFoto = "No pudimos abrir esa imagen. Prueba con otra")
            else it.copy(foto = foto, documento = null, errorFoto = null, mensaje = MENSAJE_IMAGEN_CARGADA)
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }



    fun onPdfElegido(uri: Uri) {
        val (nombre, tamano) = datosDelArchivo(uri)
        _uiState.update {
            if (tamano > MAXIMO_BYTES) it.copy(errorFoto = "El PDF es muy pesado (máximo 9 MB)")
            else it.copy(documento = ArchivoElegido(uri, nombre), foto = null, errorFoto = null)
        }
    }

    fun onGuardar() {
        val estado = _uiState.value
        if (estado.errorGeneral != null || guardando) return

        val errorTipo = if (estado.opcionElegida == null) "Indica qué documento es" else null
        val errorFoto =
            if (estado.foto == null && estado.documento == null) "Toma una foto o elige una imagen" else null
        _uiState.update { it.copy(errorTipo = errorTipo, errorFoto = errorFoto) }
        if (errorTipo != null || errorFoto != null) return

        val opcion = estado.opciones[estado.opcionElegida!!]

        guardando = true
        viewModelScope.launch {
            try {
                val archivo = prepararArchivo(estado)
                if (archivo == null) {
                    _uiState.update { it.copy(errorFoto = "No pudimos leer el archivo. Prueba con otro") }
                    return@launch
                }
                if (archivo.bytes.size > MAXIMO_BYTES) {
                    _uiState.update { it.copy(errorFoto = "El archivo es muy pesado (máximo 9 MB)") }
                    return@launch
                }

                // Si el documento coincide con una solicitud pendiente, queda vinculado a ella.
                val solicitudId = opcion.solicitudId ?: repositorio.solicitudes(idSiniestro)
                    .getOrDefault(emptyList())
                    .firstOrNull { !it.entregado && mismoDocumento(it.tipoDocumento, opcion.tipoDocumento) }?.id

                repositorio.subirDocumento(
                    idSiniestro = idSiniestro,
                    bytes = archivo.bytes,
                    nombre = archivo.nombre,
                    tipoArchivo = archivo.tipo,
                    tipoDocumento = opcion.tipoDocumento,
                    comentario = estado.comentario.trim(),
                    cargadoPor = "Asegurado",
                    solicitudId = solicitudId
                )
                    .onSuccess { _uiState.update { it.copy(guardado = true) } }
                    .onFailure { error -> _uiState.update { it.copy(errorFoto = error.message) } }
            } finally {
                guardando = false
            }
        }
    }


    private suspend fun prepararArchivo(estado: AdjuntarUiState): ArchivoParaSubir? =
        withContext(Dispatchers.IO) {
            val foto = estado.foto
            val documento = estado.documento
            try {
                when {
                    foto != null -> ArchivoParaSubir(
                        ConvertidorImagen.aBytes(foto),
                        "adjunto_${estado.numeroSiniestro}_${System.currentTimeMillis()}.jpg",
                        "image/jpeg"
                    )
                    documento != null -> {
                        val bytes = getApplication<Application>().contentResolver
                            .openInputStream(documento.uri)?.use { it.readBytes() }
                        if (bytes == null) null else ArchivoParaSubir(bytes, documento.nombre, "application/pdf")
                    }
                    else -> null
                }
            } catch (e: IOException) {
                null
            }
        }


    private fun datosDelArchivo(uri: Uri): Pair<String, Long> {
        val resolver = getApplication<Application>().contentResolver
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnaNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val columnaTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                val nombre = if (columnaNombre >= 0) cursor.getString(columnaNombre) else null
                val tamano = if (columnaTamano >= 0) cursor.getLong(columnaTamano) else 0L
                return (nombre ?: "documento.pdf") to tamano
            }
        }
        return "documento.pdf" to 0L
    }

    private fun mismoDocumento(pedido: String, enviado: String): Boolean {
        val a = sinTildes(pedido)
        val b = sinTildes(enviado)
        return a == b || a.startsWith(b) || b.startsWith(a)
    }

    private fun sinTildes(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}