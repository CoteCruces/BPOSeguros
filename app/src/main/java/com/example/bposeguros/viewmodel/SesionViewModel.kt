package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.SesionPreferencias
import com.example.bposeguros.data.UsuarioLocalRepository
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val sesion = SesionPreferencias(application)
    private val remoto = UsuarioRepository()
    private val local = UsuarioLocalRepository(application)

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()


    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        viewModelScope.launch { restaurar() }
    }


    private suspend fun restaurar() {
        val guardado = local.usuario(sesion.leer())
        if (guardado != null && !guardado.activo) sesion.borrar()
        _usuarioActual.value = guardado?.takeIf { it.activo }
        _cargando.value = false
        refrescar()
    }

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.id)
        // Se copia a Room para que los siniestros del usuario puedan relacionarse con él.
        viewModelScope.launch {
            val guardado = local.guardar(usuario)
            if (_usuarioActual.value?.id == guardado.id) _usuarioActual.value = guardado
        }
    }


    fun refrescar() {
        val actual = _usuarioActual.value ?: return
        viewModelScope.launch {
            remoto.obtener(actual.id).onSuccess { usuario ->
                if (!usuario.activo) {
                    cerrarSesion()
                } else {
                    val guardado = local.guardar(usuario)
                    if (_usuarioActual.value?.id == guardado.id) _usuarioActual.value = guardado
                }
            }
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }
}