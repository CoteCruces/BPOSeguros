package com.example.bposeguros.data

import android.content.Context
import android.database.sqlite.SQLiteException
import com.example.bposeguros.data.local.BpoDatabase
import com.example.bposeguros.data.local.aEntidad
import com.example.bposeguros.data.local.aModelo
import com.example.bposeguros.model.Usuario


class UsuarioLocalRepository(contexto: Context) {

    private val dao = BpoDatabase.obtener(contexto).usuarioDao()

    suspend fun usuario(id: Long): Usuario? = dao.buscarPorId(id)?.aModelo()


    suspend fun guardar(usuario: Usuario, reemplazarFoto: Boolean = false): Usuario {
        val foto = if (reemplazarFoto) usuario.foto else usuario.foto.ifBlank { dao.entidad(usuario.id)?.foto.orEmpty() }
        val aGuardar = usuario.copy(foto = foto)
        try {
            dao.guardar(aGuardar.aEntidad())
        } catch (e: SQLiteException) {
            // Si la copia local falla (ej. RUT repetido con otro id), la app sigue con los datos del servidor.
        }
        return aGuardar
    }
}