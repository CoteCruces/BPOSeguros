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

// =====================================================================
//  ImagenEncabezado.kt  —  IMAGEN DE res/drawable + TEXTO ENCIMA
//  ---------------------------------------------------------------------
//  1) IMÁGENES DESDE res:
//     - Copiar el .png/.jpg a app/src/main/res/drawable
//     - Nombre SOLO en minúsculas, números y guion bajo: banner_registro.png
//     - Android genera la constante R.drawable.banner_registro
//     - Se dibuja con:  Image(painter = painterResource(R.drawable.banner_registro), ...)
//
//  2) ContentScale (equivale a object-fit en CSS):
//     - Crop  -> llena el espacio y recorta lo que sobra   (object-fit: cover)
//     - Fit   -> se ve completa, puede dejar bordes vacíos (object-fit: contain)
//
//  3) BOX = contenedor que APILA sus hijos uno ENCIMA del otro (eje Z):
//        primer hijo  -> abajo  (la imagen)
//        segundo hijo -> encima (el degradado oscuro)
//        tercer hijo  -> arriba (los textos)
//     Es como position:relative + position:absolute en CSS.
//     Modifier.align(...) ubica cada hijo dentro del Box.
// =====================================================================

@Composable
fun ImagenEncabezado(
    @DrawableRes imagen: Int,
    descripcion: String,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    // Color leído desde res/values/colors.xml (forma "clásica" de Android)
    val sombra = colorResource(R.color.sombra_imagen)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.altoBanner)
    ) {
        //  la imagen
        Image(
            painter = painterResource(id = imagen),
            contentDescription = descripcion,          // accesibilidad
            contentScale = ContentScale.Crop,          // object-fit: cover
            modifier = Modifier.fillMaxSize()
        )
        //  degradado transparente -> oscuro, para que el texto se lea
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, sombra)))
        )
        //  textos abajo a la izquierda
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)          // position:absolute; bottom:0; left:0
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
            titulo = "Únete a BPO Seguros y Sigue tu siniesto",
            subtitulo = "Regístrate completando tus datos"
        )
    }
}
