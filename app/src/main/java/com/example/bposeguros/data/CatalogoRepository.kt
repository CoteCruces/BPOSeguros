package com.example.bposeguros.data

import android.content.Context
import com.example.bposeguros.data.local.BpoDatabase
import com.example.bposeguros.data.local.aModelo
import com.example.bposeguros.model.Compania
import com.example.bposeguros.model.TipoSiniestro


class CatalogoRepository(contexto: Context) {

    private val dao = BpoDatabase.obtener(contexto).catalogoDao()

    suspend fun comunas(): List<String> = dao.comunas()

    suspend fun companias(): List<Compania> = dao.companias().map { it.aModelo() }

    suspend fun tipos(): List<TipoSiniestro> = dao.tipos().map { it.aModelo() }
}