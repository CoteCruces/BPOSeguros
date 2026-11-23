package com.example.bposeguros.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded


data class UsuarioFila(
    @Embedded val usuario: UsuarioEntidad,
    @ColumnInfo(name = "nombre_rol") val rolNombre: String
)

data class SiniestroFila(
    @ColumnInfo(name = "id_siniestro") val id: Long,
    @ColumnInfo(name = "numero") val numero: String,
    @ColumnInfo(name = "id_asegurado") val aseguradoId: Long,
    @ColumnInfo(name = "nombre_asegurado") val aseguradoNombre: String,
    @ColumnInfo(name = "rut_asegurado") val aseguradoRut: String,
    @ColumnInfo(name = "id_compania") val companiaId: Long,
    @ColumnInfo(name = "nombre_compania") val companiaNombre: String,
    @ColumnInfo(name = "id_tipo") val tipoId: Long,
    @ColumnInfo(name = "nombre_tipo") val tipoNombre: String,
    @ColumnInfo(name = "fecha_ocurrencia") val fechaOcurrencia: String,
    @ColumnInfo(name = "fecha_reporte") val fechaReporte: String,
    @ColumnInfo(name = "comuna_siniestro") val comuna: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "id_etapa") val etapaId: Long,
    @ColumnInfo(name = "fecha_cambio_etapa") val fechaCambioEtapa: String,
    @ColumnInfo(name = "id_liquidador") val liquidadorId: Long?,
    @ColumnInfo(name = "nombre_liquidador") val liquidadorNombre: String?,
    @ColumnInfo(name = "equipo_liquidador") val liquidadorEquipo: String?,
    @ColumnInfo(name = "fuera_de_plazo") val fueraDePlazo: Boolean,
    @ColumnInfo(name = "resultado") val resultado: String?,
    @ColumnInfo(name = "mensaje_cierre") val mensajeCierre: String?,
    @ColumnInfo(name = "ultima_actualizacion") val ultimaActualizacion: String?
)

data class AvisoFila(
    @ColumnInfo(name = "id_aviso") val id: Long,
    @ColumnInfo(name = "id_siniestro") val siniestroId: Long,
    @ColumnInfo(name = "numero") val numeroSiniestro: String,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "motivo") val motivo: String,
    @ColumnInfo(name = "mensaje") val mensaje: String,
    @ColumnInfo(name = "leido") val leido: Boolean
)

data class CandidatoFila(
    @ColumnInfo(name = "id_usuario") val id: Long,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "codigo") val codigo: String,
    @ColumnInfo(name = "equipo") val equipo: String,
    @ColumnInfo(name = "abiertos") val casosAbiertos: Int
)