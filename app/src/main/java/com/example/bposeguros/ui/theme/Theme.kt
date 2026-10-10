package com.example.bposeguros.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary = Blue80,
    onPrimary = SobrePrimarioOscuro,
    primaryContainer = ContenedorOscuro,
    onPrimaryContainer = SobreContenedorOscuro,
    secondary = BlueGrey80,
    onSecondary = TextoOscuro,
    tertiary = Cyan80,
    onTertiary = TextoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    error = ErrorOscuro
)

private val EsquemaClaro = lightColorScheme(
    primary = Blue40,
    onPrimary = Blanco,
    primaryContainer = ContenedorClaro,
    onPrimaryContainer = SobreContenedorClaro,
    secondary = BlueGrey40,
    onSecondary = Blanco,
    tertiary = Cyan40,
    onTertiary = Blanco,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    error = ErrorClaro
)

// Esquinas redondeadas (equivale a border-radius)
private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun BPOSegurosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se deja en false para que se vea siempre nuestra paleta azul
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}