package com.example.bposeguros.viewmodel

import android.app.Application
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel(Application())
    }

    @Test
    fun onAlternarVisibilidad_muestraLaContrasena() {
        assertFalse(viewModel.uiState.value.claveVisible)

        viewModel.onAlternarVisibilidad()

        assertTrue(viewModel.uiState.value.claveVisible)
    }

    @Test
    fun onIngresar_conRutInvalido_muestraErrorEnElRut() {
        viewModel.onRutCambia("11.111.111-2")
        viewModel.onClaveCambia("Clave#2026")

        viewModel.onIngresar {}

        assertEquals("El RUT no es válido. Revisa el dígito verificador", viewModel.uiState.value.errorRut)
    }

}