package com.example.bposeguros.viewmodel

import android.app.Application
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NuevoLiquidadorViewModelTest {

    private lateinit var viewModel: NuevoLiquidadorViewModel

    @Before
    fun preparar() {
        viewModel = NuevoLiquidadorViewModel(Application())
    }

    @Test
    fun onCrear_conFormularioVacio_muestraTodosLosErrores() {
        val analista = Usuario(id = 14L, rolId = Rol.ID_ANALISTA)

        viewModel.onCrear(analista)

        assertEquals("El RUT es obligatorio", viewModel.uiState.value.errorRut)
        assertEquals("El nombre es obligatorio", viewModel.uiState.value.errorNombre)
        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
        assertEquals("Elige el equipo del liquidador", viewModel.uiState.value.errorEquipo)
    }

    @Test
    fun onCrear_siNoEsAnalista_noHaceNada() {
        val asegurado = Usuario(id = 1L, rolId = Rol.ID_ASEGURADO)

        viewModel.onCrear(asegurado)

        assertNull(viewModel.uiState.value.errorRut)
    }
}
