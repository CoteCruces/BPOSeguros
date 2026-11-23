package com.example.bposeguros.data

import com.example.bposeguros.data.remote.AvanzarEtapaRequest
import com.example.bposeguros.data.remote.CerrarSiniestroRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import com.example.bposeguros.data.remote.NuevaGestionRequest
import com.example.bposeguros.data.remote.NuevaSolicitudRequest
import com.example.bposeguros.data.remote.ReportarSiniestroRequest
import com.example.bposeguros.data.remote.SiniestrosApi
import com.example.bposeguros.data.remote.aAdjunto
import com.example.bposeguros.data.remote.aAviso
import com.example.bposeguros.data.remote.aGestion
import com.example.bposeguros.data.remote.aSiniestro
import com.example.bposeguros.data.remote.aSolicitud
import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.DetalleUiState
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class SiniestroRepository(
    private val api: SiniestrosApi = ClienteRetrofit.siniestros
) {



    suspend fun ultimosSiniestros(idAsegurado: Long, cantidad: Int = 2): Result<List<Siniestro>> =
        llamarApi { api.listar(aseguradoId = idAsegurado, orden = "reporte", limite = cantidad).map { it.aSiniestro() } }

    suspend fun siniestrosDe(idAsegurado: Long): Result<List<Siniestro>> =
        llamarApi { api.listar(aseguradoId = idAsegurado).map { it.aSiniestro() } }

    suspend fun siniestrosEntre(idAsegurado: Long, desde: String, hasta: String): Result<List<Siniestro>> =
        llamarApi { api.listar(aseguradoId = idAsegurado, desde = desde, hasta = hasta).map { it.aSiniestro() } }

    suspend fun siniestroDeAsegurado(idSiniestro: Long, idAsegurado: Long): Result<Siniestro> =
        llamarApi { api.obtener(idSiniestro, aseguradoId = idAsegurado).aSiniestro() }

    suspend fun avisos(idAsegurado: Long): Result<List<Aviso>> =
        llamarApi { api.avisos(idAsegurado).map { it.aAviso() } }

    suspend fun marcarAvisoLeido(idAviso: Long): Result<Unit> =
        llamarApi { api.marcarAvisoLeido(idAviso) }

    suspend fun reportar(
        idAsegurado: Long, idCompania: Long, idTipo: Long, fechaOcurrencia: String, comuna: String, descripcion: String
    ): Result<Siniestro> =
        llamarApi {
            api.reportar(
                ReportarSiniestroRequest(idAsegurado, idCompania, idTipo, fechaOcurrencia, comuna, descripcion)
            ).aSiniestro()
        }



    suspend fun siniestrosAsignados(idLiquidador: Long): Result<List<Siniestro>> =
        llamarApi { api.listar(liquidadorId = idLiquidador).map { it.aSiniestro() } }


    suspend fun buscarCasos(idLiquidador: Long, texto: String): Result<List<Siniestro>> =
        llamarApi { api.listar(liquidadorId = idLiquidador, buscar = texto).map { it.aSiniestro() } }


    suspend fun detalle(
        idSiniestro: Long, aseguradoId: Long?, liquidadorId: Long?, soloVisibles: Boolean
    ): Result<DetalleUiState> = llamarApi {
        val siniestro = api.obtener(idSiniestro, aseguradoId = aseguradoId, liquidadorId = liquidadorId).aSiniestro()
        DetalleUiState(
            siniestro = siniestro,
            gestiones = api.gestiones(idSiniestro, soloVisibles).map { it.aGestion() },
            solicitudes = api.solicitudes(idSiniestro).map { it.aSolicitud() },
            adjuntos = api.adjuntos(idSiniestro).map { it.aAdjunto() }
        )
    }

    suspend fun gestiones(idSiniestro: Long, soloVisibles: Boolean): Result<List<Gestion>> =
        llamarApi { api.gestiones(idSiniestro, soloVisibles).map { it.aGestion() } }

    suspend fun solicitudes(idSiniestro: Long): Result<List<SolicitudDocumento>> =
        llamarApi { api.solicitudes(idSiniestro).map { it.aSolicitud() } }

    suspend fun adjuntos(idSiniestro: Long): Result<List<DocumentoAdjunto>> =
        llamarApi { api.adjuntos(idSiniestro).map { it.aAdjunto() } }



    suspend fun registrarGestion(idSiniestro: Long, idLiquidador: Long, descripcion: String): Result<Unit> =
        llamarApi { api.crearGestion(idSiniestro, NuevaGestionRequest(idLiquidador, descripcion)); Unit }

    suspend fun solicitarDocumento(
        idSiniestro: Long, idLiquidador: Long, documento: String, comentario: String
    ): Result<Unit> =
        llamarApi { api.solicitarDocumento(idSiniestro, NuevaSolicitudRequest(idLiquidador, documento, comentario)); Unit }

    suspend fun avanzarEtapa(idSiniestro: Long, idLiquidador: Long): Result<Unit> =
        llamarApi { api.avanzarEtapa(idSiniestro, AvanzarEtapaRequest(idLiquidador)); Unit }

    suspend fun cerrar(idSiniestro: Long, idLiquidador: Long, resultado: String, mensaje: String): Result<Unit> =
        llamarApi { api.cerrar(idSiniestro, CerrarSiniestroRequest(idLiquidador, resultado, mensaje)); Unit }



    suspend fun subirDocumento(
        idSiniestro: Long,
        bytes: ByteArray,
        nombre: String,
        tipoArchivo: String,
        tipoDocumento: String,
        comentario: String,
        cargadoPor: String,
        solicitudId: Long?
    ): Result<DocumentoAdjunto> = llamarApi {
        val archivo = MultipartBody.Part.createFormData("archivo", nombre, bytes.toRequestBody(tipoArchivo.toMediaType()))
        fun texto(valor: String) = valor.toRequestBody("text/plain".toMediaType())
        api.subirAdjunto(
            idSiniestro, archivo, texto(tipoDocumento), texto(comentario), texto(cargadoPor),
            solicitudId?.let { texto(it.toString()) }
        ).aAdjunto()
    }



    suspend fun sinAsignar(): Result<List<Siniestro>> =
        llamarApi { api.listar(sinAsignar = true).map { it.aSiniestro() } }
}