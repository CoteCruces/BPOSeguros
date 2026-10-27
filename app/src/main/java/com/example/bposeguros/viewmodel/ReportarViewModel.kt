package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bposeguros.data.SiniestroDbHelper
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.model.ReportarUiState
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReportarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiniestroDbHelper.obtener(application)
    private val todosLosTipos = db.listarTipos()

    private val _uiState = MutableStateFlow(
        ReportarUiState(
            companias = db.listarCompanias().filter { it.activa },
            comunas = db.listarComunas()
        )
    )
    val uiState: StateFlow<ReportarUiState> = _uiState.asStateFlow()

    fun onCompaniaElegida(posicion: Int) {
        val compania = _uiState.value.companias[posicion]
        _uiState.update {
            it.copy(
                companiaElegida = posicion,
                tipos = todosLosTipos.filter { tipo -> tipo.linea in compania.lineas },
                tipoElegido = null,
                errorCompania = null
            )
        }
    }

    fun onTipoElegido(posicion: Int) {
        _uiState.update { it.copy(tipoElegido = posicion, errorTipo = null) }
    }

    fun onFechaCambia(valor: String) {
        _uiState.update { it.copy(fechaOcurrencia = valor, errorFecha = null) }
    }

    fun onComunaElegida(posicion: Int) {
        _uiState.update { it.copy(comuna = it.comunas[posicion], errorComuna = null) }
    }

    fun onDescripcionCambia(valor: String) {
        _uiState.update { it.copy(descripcion = valor.take(500), errorDescripcion = null) }
    }

    fun onCancelarDuplicado() {
        _uiState.update { it.copy(posibleDuplicado = null) }
    }

    fun onEnviar(idAsegurado: Long, confirmado: Boolean = false) {
        val estado = _uiState.value
        val fechaBd = Fechas.desdeTextoUsuario(estado.fechaOcurrencia)
        val hoy = Fechas.hoy()

        val errorCompania = if (estado.companiaElegida == null) "Elige tu compañía de seguros" else null
        val errorTipo = if (estado.tipoElegido == null) "Elige qué te pasó" else null

        val errorFecha = when {
            estado.fechaOcurrencia.isBlank() -> "Escribe la fecha en que ocurrió"
            fechaBd == null -> "Escribe la fecha como día-mes-año, por ejemplo 02-09-2026"
            fechaBd > hoy -> "La fecha no puede ser posterior a hoy"
            else -> null
        }

        val errorComuna = when {
            estado.comuna.isBlank() -> "Elige la comuna de la lista"
            estado.comuna !in estado.comunas -> "Esa comuna no está en la lista. Elige una de las opciones"
            else -> null
        }

        val errorDescripcion = when {
            estado.descripcion.isBlank() -> "Cuéntanos brevemente qué pasó"
            estado.descripcion.trim().length < 10 -> "Escribe un poco más (mínimo 10 caracteres)"
            else -> null
        }

        _uiState.update {
            it.copy(
                errorCompania = errorCompania, errorTipo = errorTipo, errorFecha = errorFecha,
                errorComuna = errorComuna, errorDescripcion = errorDescripcion
            )
        }
        val hayErrores = listOf(errorCompania, errorTipo, errorFecha, errorComuna, errorDescripcion).any { it != null }
        if (hayErrores || fechaBd == null) return

        val compania = estado.companias[estado.companiaElegida!!]
        val tipo = estado.tipos[estado.tipoElegido!!]

        if (!confirmado) {
            val duplicado = db.buscarPosibleDuplicado(idAsegurado, compania.id, tipo.id, fechaBd)
            if (duplicado != null) {
                _uiState.update { it.copy(posibleDuplicado = duplicado) }
                return
            }
        }

        val numero = db.siguienteNumeroSiniestro(hoy.take(4).toInt())
        val nuevo = Siniestro(
            numero = numero,
            aseguradoId = idAsegurado,
            companiaId = compania.id,
            tipoId = tipo.id,
            fechaOcurrencia = fechaBd,
            fechaReporte = hoy,
            comuna = estado.comuna,
            descripcion = estado.descripcion.trim(),
            etapa = Etapa.RECIBIDO,
            fechaCambioEtapa = hoy,
            fueraDePlazo = Fechas.diasEntre(fechaBd, hoy) > 30
        )
        val id = db.insertarSiniestro(nuevo, Fechas.ahora())
        if (id != -1L) {
            _uiState.update { it.copy(posibleDuplicado = null, numeroCreado = numero) }
        }
    }
}
