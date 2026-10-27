package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SesionPreferencias
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// NUEVO (SQLite + SharedPreferences): ViewModel -> AndroidViewModel.
// La BD y las preferencias necesitan un Context; AndroidViewModel recibe la Application.
// viewModel() en AppNavegacion lo sigue creando igual: no hace falta un Factory.
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    // NUEVO (SQLite): acceso a las tablas (el helper se comparte con los demás ViewModels)
    private val db = SiniestroDbHelper.obtener(application)

    // NUEVO (SharedPreferences): acceso a la sesión guardada
    private val sesion = SesionPreferencias(application)

    // NUEVO (SharedPreferences + SQLite): id guardado -> se busca ese usuario en la BD
    private val _usuarioActual = MutableStateFlow(db.buscarUsuarioPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // El usuario ya existe en la BD (lo creó el liquidador o viene en los datos de ejemplo).
    // Aquí solo se deja como usuario actual y se recuerda.
    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        // NUEVO (SharedPreferences): se recuerda quién inició sesión
        sesion.guardar(usuario.id)
    }

    // Vuelve a leer al usuario desde la BD (después de editar el perfil o cambiar la contraseña),
    // así el nombre y la foto nuevos se ven al tiro en todas las pantallas.
    fun refrescar() {
        val actual = _usuarioActual.value ?: return
        _usuarioActual.value = db.buscarUsuarioPorId(actual.id)
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        // NUEVO (SharedPreferences): se borra la sesión guardada
        sesion.borrar()
    }
}
