package com.example.bposeguros.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AdjuntarViewModelTest {

    private lateinit var viewModel: AdjuntarViewModel

    @Before
    fun preparar() {
        viewModel = AdjuntarViewModel(Application())
    }

    @Test
    fun onGuardar_sinDocumentoNiFoto_muestraLosErrores() {
        viewModel.onGuardar()

        assertEquals("Indica qué documento es", viewModel.uiState.value.errorTipo)
        assertEquals("Toma una foto o elige una imagen", viewModel.uiState.value.errorFoto)
    }

    @Test
    fun onComentarioCambia_conTextoLargo_loCortaEn300() {
        viewModel.onComentarioCambia("a".repeat(350))

        assertEquals(300, viewModel.uiState.value.comentario.length)
    }
}
