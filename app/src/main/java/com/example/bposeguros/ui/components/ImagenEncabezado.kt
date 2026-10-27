package com.example.bposeguros.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.bposeguros.R
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Blanco
import com.example.bposeguros.ui.theme.Dimens


@Composable
fun ImagenEncabezado(
    @DrawableRes imagen: Int,
    descripcion: String,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    val sombra = colorResource(R.color.sombra_imagen)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.altoBanner)
    ) {
        Image(
            painter = painterResource(id = imagen),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, sombra)))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Dimens.espacioPantalla)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge, color = Blanco)
            Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium, color = Blanco)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagenEncabezadoPreview() {
    BPOSegurosTheme() {
        ImagenEncabezado(
            imagen = R.drawable.banner_registro_bpo,
            descripcion = "",
            titulo = "Crea la cuenta de un cliente",
            subtitulo = "El cliente completará el resto de sus datos al entrar"
        )
    }
}
