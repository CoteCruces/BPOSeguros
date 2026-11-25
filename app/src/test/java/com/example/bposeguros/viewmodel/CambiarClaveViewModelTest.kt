package com.example.bposeguros.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CambiarClaveViewModelTest {
    private lateinit var viewModel: CambiarClaveViewModel

    @Before
    fun preparar() {
        viewModel = CambiarClaveViewModel(Application())
    }

    @Test
    fun onGuardar_conClaveMuyCorta_muestraError() {
        viewModel.onActualCambia("Clave#2025")
        viewModel.onNuevaCambia("Ab#1")

        viewModel.onGuardar(idUsuario = 1L)

        assertEquals("Debe tener al menos 8 caracteres", viewModel.uiState.value.errorNueva)
    }

    @Test
    fun onGuardar_conClaveSinMayuscula_muestraError() {
        viewModel.onActualCambia("Clave#2025")
        viewModel.onNuevaCambia("clave#2026")

        viewModel.onGuardar(idUsuario = 1L)

        assertEquals("Debe incluir al menos una mayúscula", viewModel.uiState.value.errorNueva)
    }
}