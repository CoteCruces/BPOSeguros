package com.example.bposeguros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bposeguros.R
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.ui.components.BarraSuperior
import com.example.bposeguros.ui.components.TarjetaSiniestro
import com.example.bposeguros.ui.theme.Dimens
import com.example.bposeguros.ui.util.Fechas

// =====================================================================
//  ResultadosScreen.kt  —  Siniestros encontrados por el asegurado
//  desde y hasta = "aaaa-mm-dd" -> los que OCURRIERON entre esas dos fechas (incluidas)
//  desde y hasta vacías         -> todos sus siniestros (botón "Ver todos")
//  LazyColumn solo dibuja las tarjetas que se ven: funciona bien aunque sean muchas.
// =====================================================================
@Composable
fun ResultadosScreen(
    desde: String,
    hasta: String,
    siniestros: List<Siniestro>,
    onVolver: () -> Unit,
    onAbrir: (Long) -> Unit
) {
    val verTodos = desde.isBlank() || hasta.isBlank()
    val titulo = when {
        verTodos -> stringResource(R.string.titulo_todos)
        desde == hasta -> stringResource(R.string.titulo_por_fecha, Fechas.paraMostrar(desde))   // un solo día
        else -> stringResource(R.string.titulo_entre_fechas, Fechas.paraMostrar(desde), Fechas.paraMostrar(hasta))
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = titulo, onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                Text(
                    stringResource(R.string.texto_cantidad_resultados, siniestros.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (siniestros.isEmpty()) {
                item {
                    Text(
                        stringResource(if (verTodos) R.string.sin_siniestros else R.string.sin_resultados_fecha),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(siniestros, key = { it.id }) { siniestro ->
                TarjetaSiniestro(
                    siniestro = siniestro,
                    onClick = { onAbrir(siniestro.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
