package com.example.bposeguros.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.bposeguros.R
import com.example.bposeguros.data.ConvertidorImagen
import com.example.bposeguros.ui.theme.BPOSegurosTheme
import com.example.bposeguros.ui.theme.Dimens


@Composable
fun AvatarUsuario(
    ruta: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar,
    fotoNueva: Bitmap? = null,
    conBorde: Boolean = true
) {
    val guardada = remember(ruta) { ConvertidorImagen.leer(ruta) }
    val foto = fotoNueva ?: guardada

    val forma = modifier
        .size(tamano)
        .clip(CircleShape)
        .then(
            if (conBorde) Modifier.border(BorderStroke(Dimens.bordeAvatar, MaterialTheme.colorScheme.primary), CircleShape)
            else Modifier
        )

    if (foto != null) {
        Image(
            bitmap = foto.asImageBitmap(),
            contentDescription = stringResource(R.string.cd_avatar),
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        Image(
            painter = painterResource(R.drawable.usuario),
            contentDescription = stringResource(R.string.cd_avatar),
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    }
}

@Composable
fun DialogoCerrarSesion(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
        title = { Text(stringResource(R.string.titulo_cerrar_sesion)) },
        text = { Text(stringResource(R.string.texto_cerrar_sesion)) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(stringResource(R.string.btn_cerrar_sesion)) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.btn_cancelar)) } }
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarUsuarioPreview() {
    BPOSegurosTheme {
        AvatarUsuario(ruta = "")
    }
}
