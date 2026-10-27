package com.example.bposeguros.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bposeguros.MainActivity
import com.example.bposeguros.R
import com.example.bposeguros.permisos.Permisos

// =====================================================================
//  Notificador.kt  —  RECURSO NATIVO: notificaciones del sistema (RF-09 / HU-07)
//  ---------------------------------------------------------------------
//  Cuando el liquidador cambia la etapa, pide un documento o cierra el caso,
//  además de guardar el aviso en la BD se muestra una notificación en el teléfono.
//  Así el asegurado "se entera sin tener que revisar ni llamar".
//
//   1) Crear un CANAL (obligatorio desde Android 8). Se hace al abrir la app.
//   2) Armar la notificación con NotificationCompat.Builder (ícono, título, texto).
//   3) Mostrarla con NotificationManagerCompat, solo si hay permiso.
// =====================================================================
object Notificador {

    private const val ID_CANAL = "avisos_siniestro"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL,
                context.getString(R.string.canal_nombre),
                NotificationManager.IMPORTANCE_DEFAULT      // importancia normal: sin urgencia innecesaria
            ).apply { description = context.getString(R.string.canal_descripcion) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    // idSiniestro se usa como id de la notificación: un aviso nuevo del mismo caso reemplaza al anterior
    fun mostrar(context: Context, idSiniestro: Long, titulo: String, mensaje: String) {
        if (!Permisos.puedeNotificar(context)) return     // sin permiso: el aviso igual queda en la app

        // Al tocar la notificación se abre la app (PendingIntent = "intent para después")
        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, ID_CANAL)
            .setSmallIcon(R.drawable.ic_campana)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))   // muestra el texto completo
            .setContentIntent(abrirApp)
            .setAutoCancel(true)                                            // se borra al tocarla
            .build()

        try {
            NotificationManagerCompat.from(context).notify(idSiniestro.toInt(), notificacion)
        } catch (e: SecurityException) {
            // el usuario quitó el permiso justo ahora: no se muestra, pero la app no se cae
        }
    }
}
