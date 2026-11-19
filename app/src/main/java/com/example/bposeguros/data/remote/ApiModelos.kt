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