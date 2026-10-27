package com.example.bposeguros.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File

// =====================================================================
//  ConvertidorImagen.kt  —  Fotos de evidencia (HU-05)
//
//   Cámara ──▶ Bitmap ─┐
//                       ├─▶ archivo JPEG en la carpeta privada de la app ──▶ su RUTA va a SQLite
//   Galería ─▶ Uri ────┘
//
//  A diferencia del avatar del profesor (BLOB en la tabla), aquí la foto se guarda
//  como ARCHIVO y la tabla documentos_adjuntos solo guarda la ruta (columna ruta_archivo).
//  filesDir = carpeta privada de la app: otras apps no pueden leerla (RNF-07).
// =====================================================================
object ConvertidorImagen {

    private const val LADO_MAXIMO = 1280     // suficiente para que el daño se vea bien
    private const val CALIDAD_JPEG = 85
    const val MAXIMO_KB = 5 * 1024            // RN-10: hasta 5 MB por adjunto

    // Galería: Uri -> Bitmap
    fun desdeUri(context: Context, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val origen = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(origen) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri).use { flujo -> BitmapFactory.decodeStream(flujo) }
        }
    } catch (e: Exception) {
        null    // archivo dañado o que no es imagen
    }

    // Guarda el Bitmap como JPEG en la carpeta privada. Devuelve el archivo creado (o null si falla).
    fun guardar(context: Context, bitmap: Bitmap, nombre: String): File? = try {
        val archivo = File(context.filesDir, nombre)
        archivo.outputStream().use { salida ->
            achicar(bitmap).compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        }
        archivo
    } catch (e: Exception) {
        null
    }

    // Para MOSTRAR una foto guardada. Los datos de ejemplo no tienen archivo: devuelve null.
    fun leer(ruta: String): Bitmap? =
        if (ruta.isBlank() || !File(ruta).exists()) null else BitmapFactory.decodeFile(ruta)

    // Reduce el tamaño manteniendo la proporción (no deforma la foto)
    private fun achicar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true
        )
    }
}
