package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SesionPreferencias
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)

    private val sesion = SesionPreferencias(application)

    private val _usuarioActual = MutableStateFlow(cargarSesionActiva())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.id)
    }

    fun refrescar() {
        val actual = _usuarioActual.value ?: return
        val usuario = db.buscarUsuarioPorId(actual.id)
        if (usuario != null && !usuario.activo) cerrarSesion() else _usuarioActual.value = usuario
    }
    private fun cargarSesionActiva(): Usuario? {
        val usuario = db.buscarUsuarioPorId(sesion.leer())
        if (usuario != null && !usuario.activo) {
            sesion.borrar()
            return null
        }
        return usuario
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }
}
