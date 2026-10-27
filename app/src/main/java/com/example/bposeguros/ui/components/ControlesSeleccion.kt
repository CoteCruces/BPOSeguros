package com.example.bposeguros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens

@Composable
fun CasillaConTexto(
    texto: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = marcado, onValueChange = onCambio, role = Role.Checkbox)
            .padding(vertical = Dimens.espacioMini),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = marcado, onCheckedChange = null)
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = Dimens.espacioChico)
        )
    }
}

@Composable
fun InterruptorConTexto(
    texto: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = activado, onValueChange = onCambio, role = Role.Switch)
            .padding(vertical = Dimens.espacioMini),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = activado, onCheckedChange = null)
    }
}


@Preview(showBackground = true)
@Composable
private fun ControlesSeleccionPreview() {
    BPOSegurosTheme() {
        Column(Modifier.padding(16.dp)) {
            CasillaConTexto(texto = "Acepto los términos", marcado = true, onCambio = {})
            InterruptorConTexto(texto = "Recibir novedades", activado = false, onCambio = {})
        }
    }
}
