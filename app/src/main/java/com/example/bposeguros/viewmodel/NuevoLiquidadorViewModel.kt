package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.EnvioCorreo
import com.example.bposeguros.data.MotivoClave
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.data.UsuarioRepository
import com.example.bposeguros.model.Equipo
import com.example.bposeguros.model.NuevoLiquidadorUiState
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Usuario
import com.example.bposeguros.ui.util.Seguridad
import com.example.bposeguros.ui.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NuevoLiquidadorViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()

    private val _uiState = MutableStateFlow(NuevoLiquidadorUiState())
    val uiState: StateFlow<NuevoLiquidadorUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) =
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }

    fun onNombreCambia(valor: String) =
        _uiState.update { it.copy(nombre = valor, errorNombre = null, errorGeneral = null) }

    fun onCorreoCambia(valor: String) =
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }

    fun onEquipoElegido(indice: Int) =
        _uiState.update { it.copy(equipoElegido = indice, errorEquipo = null, errorGeneral = null) }

    fun onCrear(analista: Usuario) {
        if (analista.rolId != Rol.ID_ANALISTA) return
        val estado = _uiState.value
        if (estado.enviando) return

        val rutLimpio = Validaciones.limpiarRut(estado.rut)
        val errorRut = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !Validaciones.rutValido(estado.rut) -> "El RUT no es válido. Revisa el dígito verificador"
            else -> null
        }
        val errorNombre = Validaciones.errorNombreCompleto(estado.nombre)
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        val errorEquipo = if (estado.equipoElegido == null) "Elige el equipo del liquidador" else null

        _uiState.update {
            it.copy(
                errorRut = errorRut, errorNombre = errorNombre, errorCorreo = errorCorreo,
                errorEquipo = errorEquipo, errorGeneral = null
            )
        }
        if (listOf(errorRut, errorNombre, errorCorreo, errorEquipo).any { it != null }) return

        val equipo = Equipo.lista[estado.equipoElegido!!]
        val nombre = estado.nombre.trim().replace(Regex("\\s+"), " ")

        _uiState.update { it.copy(enviando = true) }
        viewModelScope.launch {
            repositorio.crearLiquidador(analista.id, rutLimpio, nombre, estado.correo, equipo)
                .onSuccess { creado ->
                    _uiState.update {
                        it.copy(enviando = false, liquidadorCreado = true, codigoCreado = creado.codigo)
                    }
                }
                .onFailure { error ->
                    val mensaje = error.message.orEmpty()
                    _uiState.update {
                        when {
                            mensaje.startsWith("Ya existe") && mensaje.contains("RUT") ->
                                it.copy(enviando = false, errorRut = mensaje)
                            mensaje.startsWith("Ya existe") && mensaje.contains("correo") ->
                                it.copy(enviando = false, errorCorreo = mensaje)
                            else -> it.copy(enviando = false, errorGeneral = mensaje)
                        }
                    }
                }
        }
    }

    fun onLimpiar() {
        _uiState.value = NuevoLiquidadorUiState()
    }
}