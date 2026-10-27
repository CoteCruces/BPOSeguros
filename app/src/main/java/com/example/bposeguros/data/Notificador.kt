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

object Notificador {

    private const val ID_CANAL = "avisos_siniestro"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL,
                context.getString(R.string.canal_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.canal_descripcion) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun mostrar(context: Context, idSiniestro: Long, titulo: String, mensaje: String) {
        if (!Permisos.puedeNotificar(context)) return

        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, ID_CANAL)
            .setSmallIcon(R.drawable.ic_campana)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(idSiniestro.toInt(), notificacion)
        } catch (e: SecurityException) {
        }
    }
}
