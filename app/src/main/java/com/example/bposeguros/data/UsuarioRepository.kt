package com.example.bposeguros.data

import com.example.bposeguros.data.remote.ActualizarPerfilRequest
import com.example.bposeguros.data.remote.CambiarContrasenaRequest
import com.example.bposeguros.data.remote.ClienteRetrofit
import com.example.bposeguros.data.remote.CrearLiquidadorRequest
import com.example.bposeguros.data.remote.LoginRequest
import com.example.bposeguros.data.remote.RecuperarClaveRequest
import com.example.bposeguros.data.remote.RegistroAseguradoRequest
import com.example.bposeguros.data.remote.UsuarioApi
import com.example.bposeguros.data.remote.aUsuario
import com.example.bposeguros.model.Usuario


class UsuarioRepository(
    private val api: UsuarioApi = ClienteRetrofit.usuarios
) {

    suspend fun iniciarSesion(rut: String, contrasena: String): Result<Usuario> =
        llamarApi { api.iniciarSesion(LoginRequest(rut, contrasena)).aUsuario() }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String): Result<Unit> =
        llamarApi { api.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun recuperarClave(correo: String): Result<Unit> =
        llamarApi { api.recuperarClave(RecuperarClaveRequest(correo)); Unit }

    suspend fun obtener(id: Long): Result<Usuario> =
        llamarApi { api.obtener(id).aUsuario() }

    suspend fun crearAsegurado(solicitanteId: Long, rut: String, correo: String): Result<Usuario> =
        llamarApi { api.crearAsegurado(RegistroAseguradoRequest(solicitanteId, rut, correo)).aUsuario() }

    suspend fun crearLiquidador(
        solicitanteId: Long, rut: String, nombre: String, correo: String, equipo: String
    ): Result<Usuario> =
        llamarApi {
            api.crearLiquidador(CrearLiquidadorRequest(solicitanteId, rut, nombre, correo, equipo)).aUsuario()
        }

    suspend fun actualizarPerfil(
        id: Long, nombre: String, correo: String, telefono: String, comuna: String, fechaNacimiento: String
    ): Result<Usuario> =
        llamarApi {
            api.actualizarPerfil(id, ActualizarPerfilRequest(nombre, correo, telefono, comuna, fechaNacimiento)).aUsuario()
        }
}