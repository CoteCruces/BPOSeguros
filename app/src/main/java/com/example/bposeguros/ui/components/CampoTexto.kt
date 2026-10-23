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
//  ayuda: texto de ejemplo bajo el campo; se muestra solo si no hay error.
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
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1,
    //soloMarcador: Boolean = false
) {
    // Si hay error se muestra el error; si no, el texto de ayuda (ejemplo)
    //val textoApoyo = error ?: ayuda

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
        supportingText = {
            when {
                error != null -> Text(error)
                ayuda != null -> Text(ayuda)
            }
        },
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = if (lineas == 1) ImeAction.Next else ImeAction.Default
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
            valor = "15.842.371-4",
            onValorCambia = {},
            etiqueta = "RUT",
            icono = R.drawable.ic_rut,
            error = "El RUT no es válido. Revisa el dígito verificador",
            modifier = Modifier.padding(16.dp)
            
        )
    }
}