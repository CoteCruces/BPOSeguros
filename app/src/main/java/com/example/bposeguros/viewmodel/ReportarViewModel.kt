package com.example.bposeguros.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bposeguros.data.CatalogoRepository
import com.example.bposeguros.data.SiniestroRepository
import com.example.bposeguros.data.avisoCorto
import com.example.bposeguros.model.ReportarUiState
import com.example.bposeguros.model.TipoSiniestro
import com.example.bposeguros.ui.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportarViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val DIAS_MAXIMOS_REPORTE = 180
    }

    private val catalogo = CatalogoRepository(application)
    private val repositorio = SiniestroRepository()

    private var todosLosTipos: List<TipoSiniestro> = emptyList()
    private var enviando = false

    private val _uiState = MutableStateFlow(ReportarUiState())
    val uiState: StateFlow<ReportarUiState> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            todosLosTipos = catalogo.tipos()
            val companias = catalogo.companias().filter { it.activa }
            val comunas = catalogo.comunas()
            _uiState.update { it.copy(companias = companias, comunas = comunas) }
        }
    }

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
        if (enviando) return
        val estado = _uiState.value
        val fechaBd = Fechas.desdeTextoUsuario(estado.fechaOcurrencia)
        val hoy = Fechas.hoy()
        val limiteReporte = Fechas.haceDias(DIAS_MAXIMOS_REPORTE)

        val errorCompania = if (estado.companiaElegida == null) "Elige tu compañía de seguros" else null
        val errorTipo = if (estado.tipoElegido == null) "Elige qué te pasó" else null

        val errorFecha = when {
            estado.fechaOcurrencia.isBlank() -> "Elige en el calendario la fecha en que ocurrió"
            fechaBd == null -> "Elige la fecha en el calendario"
            fechaBd > hoy -> "La fecha no puede ser posterior a hoy"
            fechaBd < limiteReporte -> "Solo puedes reportar siniestros ocurridos en los últimos $DIAS_MAXIMOS_REPORTE días"
            else -> null
        }

        val errorComuna = when {
            estado.comuna.isBlank() -> "Elige la comuna de la lista"
            estado.comuna !in estado.comunas -> "Esa comuna no está en la lista. Elige una de las opciones"
            else -> null
        }

        val errorDescripcion = when {
            estado.descripcion.isBlank() -> "Cuéntanos brevemente qué pasó"
            estado.descripcion.trim().length < 20 -> "Escribe un poco más (mínimo 20 caracteres)"
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

        enviando = true
        viewModelScope.launch {
            try {
                repositorio.reportar(
                    idAsegurado, compania.id, tipo.id, fechaBd, estado.comuna, estado.descripcion.trim()
                )
                    .onSuccess { siniestro ->
                        _uiState.update { it.copy(posibleDuplicado = null, numeroCreado = siniestro.numero) }
                    }
                    .onFailure { error ->

                        getApplication<Application>().avisoCorto(error.message.orEmpty())
                    }
            } finally {
                enviando = false
            }
        }
    }
}