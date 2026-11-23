package com.example.bposeguros.data.remote

import com.example.bposeguros.model.Usuario


data class LoginRequest(val rut: String, val contrasena: String)
data class RegistroAseguradoRequest(val solicitanteId: Long, val rut: String, val correo: String)
data class CrearLiquidadorRequest(val solicitanteId: Long, val rut: String, val nombre: String, val correo: String, val equipo: String)
data class RecuperarClaveRequest(val correo: String)
data class ActualizarPerfilRequest(val nombre: String, val correo: String, val telefono: String, val comuna: String, val fechaNacimiento: String)
data class CambiarContrasenaRequest(val actual: String, val nueva: String)
data class CambiarEstadoRequest(val solicitanteId: Long, val activo: Boolean)

data class UsuarioResponse(
    val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val comuna: String,
    val telefono: String,
    val fechaNacimiento: String,
    val codigo: String,
    val equipo: String,
    val activo: Boolean,
    val claveTemporal: Boolean,
    val perfilCompleto: Boolean,
    val rolId: Long,
    val rolNombre: String,
    val avatarUrl: String?
)


fun UsuarioResponse.aUsuario() = Usuario(
    id = id, nombre = nombre, rut = rut, correo = correo, comuna = comuna,
    telefono = telefono, fechaNacimiento = fechaNacimiento,
    foto = avatarUrl.orEmpty(),
    claveTemporal = claveTemporal, codigo = codigo, equipo = equipo,
    activo = activo, rolId = rolId, rolNombre = rolNombre
)




data class SolicitanteRequest(val solicitanteId: Long)

data class ConteoCasosResponse(val liquidadorId: Long, val casosAbiertos: Long)

data class HistorialResponse(
    val asignacionId: Long?,
    val siniestroId: Long,
    val numero: String,
    val aseguradoNombre: String,
    val companiaNombre: String,
    val tipoNombre: String,
    val etapa: String,
    val etapaEtiqueta: String,
    val resultado: String?,
    val fechaInicio: String,
    val fechaFin: String?,
    val diasTrabajados: Long,
    val vigente: Boolean,
    val motivoFin: String?
)

data class SiniestroRespuesta(
    val id: Long,
    val numero: String,
    val aseguradoId: Long,
    val aseguradoNombre: String,
    val aseguradoRut: String,
    val companiaId: Long,
    val companiaNombre: String,
    val tipoId: Long,
    val tipoNombre: String,
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val comuna: String,
    val descripcion: String,
    val etapaId: Int,
    val etapa: String,
    val etapaEtiqueta: String,
    val fechaCambioEtapa: String?,
    val liquidadorId: Long?,
    val liquidadorNombre: String?,
    val liquidadorCodigo: String?,
    val liquidadorEquipo: String?,
    val fechaAsignacion: String?,
    val diasAsignado: Long?,
    val liquidadorAnteriorId: Long?,
    val liquidadorAnteriorNombre: String?,
    val porReasignar: Boolean,
    val fueraDePlazo: Boolean,
    val resultado: String?,
    val mensajeCierre: String?,
    val ultimaActualizacion: String?
)

data class LiquidadorPropuesto(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val equipo: String,
    val casosAbiertos: Long
)

data class AsignarSiniestroRequest(
    val solicitanteId: Long,
    val liquidadorId: Long
)



data class GestionResponse(
    val id: Long,
    val siniestroId: Long,
    val fechaHora: String,
    val tipo: String,
    val descripcion: String,
    val responsable: String,
    val visibleAsegurado: Boolean
)

data class SolicitudResponse(
    val id: Long,
    val siniestroId: Long,
    val documentoSolicitado: String,
    val comentario: String?,
    val fechaSolicitud: String,
    val entregado: Boolean,
    val fechaEntrega: String?
)

data class AdjuntoRespuesta(
    val id: Long,
    val siniestroId: Long,
    val nombreArchivo: String,
    val tipoDocumento: String,
    val fechaCarga: String,
    val cargadoPor: String,
    val comentario: String?,
    val solicitudId: Long?,
    val urlArchivo: String?
)

data class AvisoRespuesta(
    val id: Long,
    val siniestroId: Long,
    val numeroSiniestro: String,
    val fechaHora: String,
    val motivo: String,
    val mensaje: String,
    val leido: Boolean
)

data class ReportarSiniestroRequest(
    val aseguradoId: Long,
    val companiaId: Long,
    val tipoId: Long,
    val fechaOcurrencia: String,
    val comuna: String,
    val descripcion: String
)

data class NuevaGestionRequest(
    val liquidadorId: Long,
    val descripcion: String
)


data class NuevaSolicitudRequest(
    val liquidadorId: Long,
    val documento: String,
    val comentario: String
)


data class AvanzarEtapaRequest(
    val liquidadorId: Long
)


data class CerrarSiniestroRequest(
    val liquidadorId: Long,
    val resultado: String,
    val mensaje: String
)