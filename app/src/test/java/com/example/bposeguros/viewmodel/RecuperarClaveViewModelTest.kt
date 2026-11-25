package com.example.bposeguros.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class RecuperarClaveViewModelTest {
    private lateinit var viewModel: RecuperarClaveViewModel

    @Before
    fun preparar() {
        viewModel = RecuperarClaveViewModel(Application())
    }

    @Test
    fun onEnviar_sinCorreo_muestraQueEsObligatorio() {
        viewModel.onEnviar()

        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onEnviar_conCorreoMalEscrito_muestraError() {
        viewModel.onCorreoCambia("camila.correo.cl")

        viewModel.onEnviar()

        assertEquals(
            "Ingresa un correo válido (ejemplo: usuario@dominio.com)",
            viewModel.uiState.value.errorCorreo
        )
    }


}