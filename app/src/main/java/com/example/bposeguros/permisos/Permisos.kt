package com.example.bposeguros.permisos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

// =====================================================================
//  Permisos.kt  —  Los 4 pasos para usar un permiso PELIGROSO
//   1) DECLARAR   <uses-permission> en AndroidManifest.xml
//   2) VERIFICAR  ¿ya está concedido?            -> tiene()
//   3) PEDIR      diálogo del sistema             -> RequestPermission (en la pantalla)
//   4) REACCIONAR concedido -> usar la API / rechazado -> avisar y ofrecer Ajustes
// =====================================================================
object Permisos {

    val CAMARA: String = Manifest.permission.CAMERA

    // El permiso para leer fotos cambió en Android 13 (API 33)
    val IMAGENES: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

    // Las notificaciones necesitan permiso SOLO desde Android 13.
    // En versiones anteriores vienen permitidas, por eso no hay nada que pedir.
    val necesitaPermisoNotificaciones: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    const val NOTIFICACIONES: String = "android.permission.POST_NOTIFICATIONS"

    fun tiene(context: Context, permiso: String): Boolean =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    fun puedeNotificar(context: Context): Boolean =
        !necesitaPermisoNotificaciones || tiene(context, NOTIFICACIONES)

    // Abre los Ajustes de ESTA app (si el usuario rechazó 2 veces, es la única forma de conceder)
    fun abrirAjustes(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
