package com.example.bposeguros.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.bposeguros.ui.theme.Blanco
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.theme.GrisDeshabilitado

// =====================================================================
//  EstilosBoton.kt  —  Estilos de botones en UN solo lugar.
//  Si se cambia aquí, cambian TODOS los botones de la app.
// =====================================================================
object EstilosBoton {
    val forma: Shape = RoundedCornerShape(Dimens.radioBoton)
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

    // Botón principal: fondo del color principal y texto contrastante
    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )

    // Botón secundario: transparente, con texto del color principal
    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )

    // Borde del botón secundario
    @Composable
    fun bordeSecundario(): BorderStroke = BorderStroke(
        Dimens.bordeBoton,
        MaterialTheme.colorScheme.primary
    )

    // Sombra del botón principal (al presionarlo, "se hunde")
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = 1.dp
    )
}

// Altura mínima: crece si el usuario aumenta el tamaño de letra del teléfono (RNF-04)
fun Modifier.estiloAltoBoton(): Modifier = this.heightIn(min = Dimens.alturaBoton)