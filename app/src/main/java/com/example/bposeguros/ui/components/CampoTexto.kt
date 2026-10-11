package com.example.bposeguros.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.R
import com.example.bposeguros.ui.styles.EstilosCampo
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens

// =====================================================================
//  CampoTexto.kt  —  CAMPO DE TEXTO REUTILIZABLE (el <input> de HTML)
//  IMPORTANTE: en Compose un campo NO guarda su propio texto.
//   - "valor" es lo que se muestra (viene del estado del ViewModel).
//   - "onValorCambia" avisa cada vez que el usuario escribe una letra.
//  Si no se actualiza el estado en onValorCambia, el campo NO cambia.
//  Esto se llama "estado elevado" (state hoisting).
//
//  error: mensaje bajo el campo; null = sin error.
//  El error se comunica con TEXTO y con borde rojo, no solo con color.
// =====================================================================
@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = ImeAction.Next
        ),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview() {
    BPOSegurosTheme {
        CampoTexto(
            valor = "ana@",
            onValorCambia = {},
            etiqueta = "Correo electrónico",
            icono = R.drawable.ic_correo,
            error = "Ingresa un correo válido",
            modifier = Modifier.padding(16.dp)
        )
    }
}