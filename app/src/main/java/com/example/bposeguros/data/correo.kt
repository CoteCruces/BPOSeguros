package com.example.bposeguros.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

// =====================================================================
//  Correo.kt  —  Abrir la app de correo del teléfono con el mensaje ya escrito
//  ---------------------------------------------------------------------
//  Es un INTENT IMPLÍCITO, igual que la galería (ACTION_PICK): la app no sabe
//  mandar correos, así que le pide a Android "abre una app que sepa hacerlo".
//   - ACTION_SENDTO + "mailto:" -> solo aparecen apps de correo (Gmail, Outlook...)
//   - EXTRA_EMAIL, EXTRA_SUBJECT y EXTRA_TEXT -> destinatario, asunto y cuerpo
//  La persona solo revisa y presiona "Enviar". No necesita permisos.
// =====================================================================
object Correo {

    // Devuelve false si el teléfono no tiene ninguna app de correo (la pantalla lo avisa)
    fun abrir(context: Context, para: String, asunto: String, cuerpo: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(para))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
