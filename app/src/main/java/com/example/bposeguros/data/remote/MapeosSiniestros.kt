package com.example.bposeguros.data.remote

import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.CandidatoLiquidador
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.Resultado
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento

fun SiniestroRespuesta.aSiniestro() = Siniestro(
    id = id,
    numero = numero,
    aseguradoId = aseguradoId,
    aseguradoNombre = aseguradoNombre,
    aseguradoRut = aseguradoRut,
    companiaId = companiaId,
    companiaNombre = companiaNombre,
    tipoId = tipoId,
    tipoNombre = tipoNombre,
    fechaOcurrencia = fechaOcurrencia,
    fechaReporte = fechaReporte,
    comuna = comuna,
    descripcion = descripcion,
    etapa = Etapa.desdeId(etapaId.toLong()),
    fechaCambioEtapa = fechaCambioEtapa.orEmpty(),
    liquidadorId = liquidadorId,
    liquidadorNombre = liquidadorNombre,
    liquidadorEquipo = liquidadorEquipo,
    fueraDePlazo = fueraDePlazo,
    resultado = resultado?.let { r -> Resultado.entries.firstOrNull { it.name == r.uppercase() } },
    mensajeCierre = mensajeCierre,
    ultimaActualizacion = ultimaActualizacion
)

fun GestionResponse.aGestion() = Gestion(
    id = id,
    siniestroId = siniestroId,
    fechaHora = fechaHora,
    tipo = tipo,
    descripcion = descripcion,
    responsable = responsable,
    visible = visibleAsegurado
)

fun SolicitudResponse.aSolicitud() = SolicitudDocumento(
    id = id,
    siniestroId = siniestroId,
    tipoDocumento = documentoSolicitado,
    comentario = comentario.orEmpty(),
    fechaSolicitud = fechaSolicitud,
    entregado = entregado,
    fechaEntrega = fechaEntrega
)


fun AdjuntoRespuesta.aAdjunto() = DocumentoAdjunto(
    id = id,
    siniestroId = siniestroId,
    nombreArchivo = nombreArchivo,
    rutaArchivo = urlArchivo.orEmpty(),
    tipoDocumento = tipoDocumento,
    fechaCarga = fechaCarga,
    cargadoPor = cargadoPor,
    comentario = comentario.orEmpty(),
    solicitudId = solicitudId
)

fun AvisoRespuesta.aAviso() = Aviso(
    id = id,
    siniestroId = siniestroId,
    numeroSiniestro = numeroSiniestro,
    fechaHora = fechaHora,
    motivo = motivo,
    mensaje = mensaje,
    leido = leido
)

fun LiquidadorPropuesto.aCandidato() = CandidatoLiquidador(
    id = id,
    nombre = nombre,
    codigo = codigo,
    equipo = equipo,
    casosAbiertos = casosAbiertos.toInt()
)