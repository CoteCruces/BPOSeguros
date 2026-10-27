package com.example.bposeguros.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.PerfilUiState
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)
    private var idCargado = -1L
    private val comunas = db.listarComunas()

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
            comunas = comunas,
            fechaNacimiento = Fechas.paraCampo(usuario.fechaNacimiento),
            fotoActual = usuario.foto
        )
    }

    fun onNombreCambia(valor: String) = _uiState.update { it.copy(nombre = valor, errorNombre = null, guardado = false) }
    fun onCorreoCambia(valor: String) = _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, guardado = false) }
    fun onTelefonoCambia(valor: String) = _uiState.update { it.copy(telefono = valor, errorTelefono = null, guardado = false) }
    fun onComunaCambia(valor: String) = _uiState.update { it.copy(comuna = valor, errorComuna = null, guardado = false) }
    fun onFechaCambia(valor: String) = _uiState.update { it.copy(fechaNacimiento = valor, errorFecha = null, guardado = false) }

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
        val estado = _uiState.value
        val fechaBd = Fechas.desdeTextoUsuario(estado.fechaNacimiento)

        val errorFecha = when {
            estado.fechaNacimiento.isBlank() -> null
            fechaBd == null -> "Escribe la fecha como día-mes-año, por ejemplo 14-05-1990"
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
            estado.comuna !in comunas -> "Esa comuna no está en la lista. Elige una de las opciones"
            else -> null
        }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre, errorCorreo = errorCorreo, errorTelefono = errorTelefono,
                errorComuna = errorComuna, errorFecha = errorFecha
            )
        }
        if (listOf(errorNombre, errorCorreo, errorTelefono, errorComuna, errorFecha).any { it != null }) return

        var rutaFoto = estado.fotoActual
        if (estado.fotoNueva != null) {
            val archivo = ConvertidorImagen.guardar(
                getApplication(), estado.fotoNueva, "perfil_${usuario.id}_${System.currentTimeMillis()}.jpg"
            )
            if (archivo == null) {
                _uiState.update { it.copy(errorFoto = "No pudimos guardar la foto. Intenta otra vez") }
                return
            }
            rutaFoto = archivo.absolutePath
        }

        db.actualizarPerfil(
            usuario.copy(
                nombre = estado.nombre.trim(),
                correo = estado.correo,
                telefono = estado.telefono.trim(),
                comuna = estado.comuna,
                fechaNacimiento = if (fechaBd == null) "" else fechaBd,
                foto = rutaFoto
            )
        )
        _uiState.update { it.copy(fotoActual = rutaFoto, fotoNueva = null, guardado = true) }
    }
}
