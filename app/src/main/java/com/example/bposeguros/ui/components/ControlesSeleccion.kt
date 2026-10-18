package com.example.bposeguros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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

// =====================================================================
//  ControlesSeleccion.kt  —  Checkbox, Switch y RadioButton con texto.
//  ---------------------------------------------------------------------
//  Los tres usan el contenedor ROW (fila): control a un lado, texto al otro.
//     Row(verticalAlignment = Alignment.CenterVertically)  ~  display:flex; align-items:center
//
//  Truco de usabilidad: hacemos presionable TODA la fila (toggleable /
//  selectable) y al control le pasamos onCheckedChange = null. Así el
//  usuario puede tocar el texto y no solo el cuadradito.
//  Un archivo puede tener VARIOS componentes pequeños relacionados.
// =====================================================================

/** Checkbox + texto (por ejemplo, aceptar términos). */
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

/** Texto + Switch (interruptor encendido/apagado). */
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
        horizontalArrangement = Arrangement.SpaceBetween  // texto a la izquierda, switch a la derecha
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = activado, onCheckedChange = null)
    }
}

/**
 * Grupo de RadioButton: se elige UNA opción entre varias.
 * Es genérico <T>: sirve para cualquier lista (aquí la usamos con un enum).
 * @param textoDe función que convierte cada opción en el texto a mostrar.
 */
/*
@Composable
fun <T> GrupoRadio(
    opciones: List<T>,
    seleccionada: T,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier
) {
    // Column = contenedor vertical (una opción debajo de otra)
    Column(modifier = modifier.selectableGroup()) {
        opciones.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = opcion == seleccionada,
                        onClick = { onSeleccionar(opcion) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = Dimens.espacioMini),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = opcion == seleccionada, onClick = null)
                Text(
                    text = textoDe(opcion),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimens.espacioChico)
                )
            }
        }
    }
}
*/

@Preview(showBackground = true)
@Composable
private fun ControlesSeleccionPreview() {
    BPOSegurosTheme() {
        Column(Modifier.padding(16.dp)) {
            CasillaConTexto(texto = "Acepto los términos", marcado = true, onCambio = {})
            InterruptorConTexto(texto = "Recibir novedades", activado = false, onCambio = {})
            /*GrupoRadio(
                opciones = listOf("Uno", "Dos", "Tres"),
                seleccionada = "Dos",
                onSeleccionar = {},
                textoDe = { it }
            )*/
        }
    }
}
