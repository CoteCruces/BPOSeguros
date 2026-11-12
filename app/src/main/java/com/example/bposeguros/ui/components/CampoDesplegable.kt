package com.example.bposeguros.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.example.bposeguros.R
import com.example.bposeguros.ui.styles.EstilosCampo
import com.example.bposeguros.ui.theme.Dimens


@Composable
fun CampoDesplegable(
    valor: String,
    etiqueta: String,
    opciones: List<String>,
    onElegir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(etiqueta) },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_flecha_abajo),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            },
            singleLine = true,
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClickLabel = etiqueta, role = Role.DropdownList) { abierto = true }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onElegir(indice)
                        abierto = false
                    }
                )
            }
        }
    }
}