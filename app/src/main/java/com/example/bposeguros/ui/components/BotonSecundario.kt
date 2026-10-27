package com.example.bposeguros.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.ui.styles.EstilosBoton
import com.example.bposeguros.ui.styles.estiloAltoBoton
import com.example.bposeguros.ui.theme.BPOSegurosTheme

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.estiloAltoBoton(),
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresSecundario(),
        border = EstilosBoton.bordeSecundario(),
        contentPadding = EstilosBoton.relleno
    ) {
        Text(text = texto)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonSecundarioPreview() {
    BPOSegurosTheme {
        BotonSecundario(texto = "Limpiar", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
