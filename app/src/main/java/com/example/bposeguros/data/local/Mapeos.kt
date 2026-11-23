package com.example.bposeguros.data.local

import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.CandidatoLiquidador
import com.example.bposeguros.model.Compania
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.LineaSiniestro
import com.example.bposeguros.model.Resultado
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento
import com.example.bposeguros.model.TipoSiniestro
import com.example.bposeguros.model.Usuario



fun UsuarioFila.aModelo() = Usuario(
    id = usuario.id,
    nombre = usuario.nombre,
    rut = usuario.rut,
    correo = usuario.correo,
    comuna = usuario.comuna,
    telefono = usuario.telefono,
    fechaNacimiento = usuario.fechaNacimiento,
    foto = usuario.foto,
    claveTemporal = usuario.claveTemporal,
    codigo = usuario.codigo,
    equipo = usuario.equipo,
    activo = usuario.activo,
    rolId = usuario.rolId,
    rolNombre = rolNombre
)

fun Usuario.aEntidad() = UsuarioEntidad(
    id = id,
    nombre = nombre,
    rut = rut,
    correo = correo,
    claveTemporal = claveTemporal,
    comuna = comuna,
    telefono = telefono,
    fechaNacimiento = fechaNacimiento,
    foto = foto,
    codigo = codigo,
    equipo = equipo,
    activo = activo,
    rolId = rolId
)

fun CompaniaEntidad.aModelo() = Compania(
    id = id,
    codigo = codigo,
    nombre = nombre,
    lineas = buildList {
        if (gestionaVehiculos) add(LineaSiniestro.VEHICULO)
        if (gestionaHogar) add(LineaSiniestro.HOGAR)
    },
    plazoDiasHabiles = plazoDias,
    activa = activa
)

fun TipoEntidad.aModelo() = TipoSiniestro(
    id = id,
    codigo = codigo,
    nombre = nombre,
    linea = LineaSiniestro.valueOf(linea)
)

fun SiniestroFila.aModelo() = Siniestro(
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
    etapa = Etapa.desdeId(etapaId),
    fechaCambioEtapa = fechaCambioEtapa,
    liquidadorId = liquidadorId,
    liquidadorNombre = liquidadorNombre,
    liquidadorEquipo = liquidadorEquipo,
    fueraDePlazo = fueraDePlazo,
    resultado = resultado?.let { Resultado.valueOf(it) },
    mensajeCierre = mensajeCierre,
    ultimaActualizacion = ultimaActualizacion
)

fun Siniestro.aEntidad() = SiniestroEntidad(
    id = id,
    numero = numero,
    aseguradoId = aseguradoId,
    companiaId = companiaId,
    tipoId = tipoId,
    fechaOcurrencia = fechaOcurrencia,
    fechaReporte = fechaReporte,
    comuna = comuna,
    descripcion = descripcion,
    etapaId = etapa.id,
    fechaCambioEtapa = fechaCambioEtapa,
    liquidadorId = liquidadorId,
    fueraDePlazo = fueraDePlazo,
    resultado = resultado?.name,
    mensajeCierre = mensajeCierre
)

fun GestionEntidad.aModelo() = Gestion(
    id = id,
    siniestroId = siniestroId,
    fechaHora = fechaHora,
    tipo = tipo,
    descripcion = descripcion,
    responsable = responsable,
    visible = visibleAsegurado
)

fun Gestion.aEntidad() = GestionEntidad(
    id = id,
    siniestroId = siniestroId,
    fechaHora = fechaHora,
    tipo = tipo,
    descripcion = descripcion,
    responsable = responsable,
    visibleAsegurado = visible
)

fun SolicitudEntidad.aModelo() = SolicitudDocumento(
    id = id,
    siniestroId = siniestroId,
    tipoDocumento = tipoDocumento,
    comentario = comentario,
    fechaSolicitud = fechaSolicitud,
    entregado = entregado,
    fechaEntrega = fechaEntrega
)

fun SolicitudDocumento.aEntidad() = SolicitudEntidad(
    id = id,
    siniestroId = siniestroId,
    tipoDocumento = tipoDocumento,
    comentario = comentario,
    fechaSolicitud = fechaSolicitud,
    entregado = entregado,
    fechaEntrega = fechaEntrega
)

fun AdjuntoEntidad.aModelo() = DocumentoAdjunto(
    id = id,
    siniestroId = siniestroId,
    nombreArchivo = nombreArchivo,
    rutaArchivo = rutaArchivo,
    tipoDocumento = tipoDocumento,
    fechaCarga = fechaCarga,
    cargadoPor = cargadoPor,
    comentario = comentario,
    solicitudId = solicitudId
)

fun DocumentoAdjunto.aEntidad() = AdjuntoEntidad(
    id = id,
    siniestroId = siniestroId,
    nombreArchivo = nombreArchivo,
    rutaArchivo = rutaArchivo,
    tipoDocumento = tipoDocumento,
    fechaCarga = fechaCarga,
    cargadoPor = cargadoPor,
    comentario = comentario,
    solicitudId = solicitudId
)

fun AvisoFila.aModelo() = Aviso(
    id = id,
    siniestroId = siniestroId,
    numeroSiniestro = numeroSiniestro,
    fechaHora = fechaHora,
    motivo = motivo,
    mensaje = mensaje,
    leido = leido
)

fun CandidatoFila.aModelo() = CandidatoLiquidador(
    id = id,
    nombre = nombre,
    codigo = codigo,
    equipo = equipo,
    casosAbiertos = casosAbiertos
)