package com.example.bposeguros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bposeguros.data.Notificador
import com.example.bposeguros.navigation.AppNavegacion
import com.example.bposeguros.ui.theme.BPOSegurosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notificador.crearCanal(this)
        setContent {
            BPOSegurosTheme { AppNavegacion() }
        }
    }
}
