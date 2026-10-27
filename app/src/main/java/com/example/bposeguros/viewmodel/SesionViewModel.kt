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

    private val _usuarioActual = MutableStateFlow(db.buscarUsuarioPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.id)
    }

    fun refrescar() {
        val actual = _usuarioActual.value ?: return
        _usuarioActual.value = db.buscarUsuarioPorId(actual.id)
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }
}
