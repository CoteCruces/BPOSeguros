package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.SesionPreferencias
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)
    private val sesion = SesionPreferencias(application)
    private val repositorio = UsuarioRepository()

    private val _usuarioActual = MutableStateFlow(cargarSesionActiva())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    init {

        refrescar()
    }

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.id)
    }


    fun refrescar() {
        val actual = _usuarioActual.value ?: return
        viewModelScope.launch {
            repositorio.obtener(actual.id).onSuccess { usuario ->
                if (usuario.activo) _usuarioActual.value = usuario else cerrarSesion()
            }
        }
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