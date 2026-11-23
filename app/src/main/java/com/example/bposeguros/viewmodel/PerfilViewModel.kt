package com.example.bposeguros.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.CatalogoRepository
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.data.UsuarioLocalRepository
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.PerfilUiState
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val catalogo = CatalogoRepository(application)
    private val remoto = UsuarioRepository()
    private val local = UsuarioLocalRepository(application)

    private var idCargado = -1L
    private var guardando = false

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar(usuario: Usuario) {
        if (idCargado == usuario.id) return
        idCargado = usuario.id
        _uiState.value = PerfilUiState(
            nombre = usuario.nombre,
            correo = usuario.correo,
            telefono = usuario.telefono,
            comuna = usuario.comuna,
            fechaNacimiento = Fechas.paraCampo(usuario.fechaNacimiento),
            fotoActual = usuario.foto
        )
        viewModelScope.launch {
            val comunas = catalogo.comunas()
            _uiState.update { it.copy(comunas = comunas) }
        }
    }

    fun onNombreCambia(valor: String) = _uiState.update { it.copy(nombre = valor, errorNombre = null, errorGeneral = null, guardado = false) }
    fun onCorreoCambia(valor: String) = _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null, guardado = false) }
    fun onTelefonoCambia(valor: String) = _uiState.update { it.copy(telefono = valor, errorTelefono = null, errorGeneral = null, guardado = false) }
    fun onComunaCambia(valor: String) = _uiState.update { it.copy(comuna = valor, errorComuna = null, errorGeneral = null, guardado = false) }
    fun onFechaCambia(valor: String) = _uiState.update { it.copy(fechaNacimiento = valor, errorFecha = null, errorGeneral = null, guardado = false) }

    fun onFotoTomada(foto: Bitmap) {
        _uiState.update { it.copy(fotoNueva = foto, errorFoto = null, guardado = false) }
    }

    fun onImagenElegida(uri: Uri) {
        val foto = ConvertidorImagen.desdeUri(getApplication(), uri)
        _uiState.update {
            if (foto == null) it.copy(errorFoto = "No pudimos abrir esa imagen. Prueba con otra")
            else it.copy(fotoNueva = foto, errorFoto = null, guardado = false)
        }
    }

    fun onQuitarFoto() {
        _uiState.update { it.copy(fotoNueva = null, fotoActual = "", guardado = false) }
    }

    fun onGuardar(usuario: Usuario) {
        if (guardando) return
        val estado = _uiState.value
        val fechaBd = Fechas.desdeTextoUsuario(estado.fechaNacimiento)

        val errorFecha = when {
            estado.fechaNacimiento.isBlank() -> null
            fechaBd == null -> "Elige la fecha en el calendario"
            fechaBd > Fechas.hoy() -> "La fecha de nacimiento no puede ser futura"
            fechaBd > Fechas.haceAnios(18) -> "Debes tener al menos 18 años"
            fechaBd < Fechas.haceAnios(120) -> "Revisa el año: no puede ser de hace más de 120 años"
            else -> null
        }

        val palabrasNombre = estado.nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val errorNombre = when {
            estado.nombre.isBlank() -> null
            !estado.nombre.all { it.isLetter() || it == ' ' } -> "El nombre no puede tener números ni símbolos"
            palabrasNombre.size < 2 -> "Escribe tu nombre y apellido"
            palabrasNombre.any { it.length < 2 } -> "Cada palabra debe tener al menos 2 letras"
            else -> null
        }


        val errorCorreo = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !estado.correo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")) ->
                "Ingresa un correo válido (ejemplo: usuario@dominio.com)"
            else -> null
        }

        val digitosTelefono = estado.telefono.filter { it.isDigit() }
        val telefonoValido = (digitosTelefono.length == 11 && digitosTelefono.startsWith("569")) ||
                (digitosTelefono.length == 9 && digitosTelefono.startsWith("9"))
        val errorTelefono = when {
            estado.telefono.isBlank() -> null
            !telefonoValido -> "Ingresa un celular válido (ejemplo: +56 9 12345678)"
            else -> null
        }

        val errorComuna = when {
            estado.comuna.isBlank() -> null
            estado.comuna !in estado.comunas -> "Esa comuna no está en la lista. Elige una de las opciones"
            else -> null
        }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre, errorCorreo = errorCorreo, errorTelefono = errorTelefono,
                errorComuna = errorComuna, errorFecha = errorFecha, errorGeneral = null
            )
        }
        if (listOf(errorNombre, errorCorreo, errorTelefono, errorComuna, errorFecha).any { it != null }) return

        guardando = true
        viewModelScope.launch {
            try {
                var rutaFoto = estado.fotoActual
                val fotoNueva = estado.fotoNueva
                if (fotoNueva != null) {
                    val archivo = withContext(Dispatchers.IO) {
                        ConvertidorImagen.guardar(
                            getApplication(), fotoNueva, "perfil_${usuario.id}_${System.currentTimeMillis()}.jpg"
                        )
                    }
                    if (archivo == null) {
                        _uiState.update { it.copy(errorFoto = "No pudimos guardar la foto. Intenta otra vez") }
                        return@launch
                    }
                    rutaFoto = archivo.absolutePath
                }


                remoto.actualizarPerfil(
                    id = usuario.id,
                    nombre = estado.nombre.trim(),
                    correo = estado.correo,
                    telefono = estado.telefono.trim(),
                    comuna = estado.comuna,
                    fechaNacimiento = fechaBd ?: ""
                )
                    .onSuccess { actualizado ->
                        local.guardar(actualizado.copy(foto = rutaFoto), reemplazarFoto = true)
                        _uiState.update { it.copy(fotoActual = rutaFoto, fotoNueva = null, guardado = true) }
                    }
                    .onFailure { error ->
                        _uiState.update { conError(it, error.message.orEmpty()) }
                    }
            } finally {
                guardando = false
            }
        }
    }


    private fun conError(estado: PerfilUiState, mensaje: String): PerfilUiState {
        val m = mensaje.lowercase()
        return when {
            "correo" in m -> estado.copy(errorCorreo = mensaje)
            "nombre" in m || "apellido" in m || "palabra" in m -> estado.copy(errorNombre = mensaje)
            "celular" in m || "teléfono" in m || "telefono" in m -> estado.copy(errorTelefono = mensaje)
            "comuna" in m -> estado.copy(errorComuna = mensaje)
            "fecha" in m || "año" in m -> estado.copy(errorFecha = mensaje)
            else -> estado.copy(errorGeneral = mensaje)
        }
    }
}