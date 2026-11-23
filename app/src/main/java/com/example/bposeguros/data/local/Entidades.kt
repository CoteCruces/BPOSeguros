package com.example.bposeguros.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "comunas", indices = [Index(value = ["nombre_comuna"], unique = true)])
data class ComunaEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_comuna") val id: Long = 0,
    @ColumnInfo(name = "nombre_comuna") val nombre: String
)

@Entity(tableName = "roles")
data class RolEntidad(
    @PrimaryKey @ColumnInfo(name = "id_rol") val id: Long,
    @ColumnInfo(name = "nombre_rol") val nombre: String
)


@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(entity = RolEntidad::class, parentColumns = ["id_rol"], childColumns = ["id_rol"])
    ],
    indices = [Index(value = ["rut"], unique = true), Index(value = ["id_rol"])]
)
data class UsuarioEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_usuario") val id: Long = 0,
    @ColumnInfo(name = "nombre", defaultValue = "''") val nombre: String = "",
    @ColumnInfo(name = "rut") val rut: String,
    @ColumnInfo(name = "correo") val correo: String,
    @ColumnInfo(name = "clave_temporal", defaultValue = "0") val claveTemporal: Boolean = false,
    @ColumnInfo(name = "comuna", defaultValue = "''") val comuna: String = "",
    @ColumnInfo(name = "telefono", defaultValue = "''") val telefono: String = "",
    @ColumnInfo(name = "fecha_nacimiento", defaultValue = "''") val fechaNacimiento: String = "",
    @ColumnInfo(name = "foto", defaultValue = "''") val foto: String = "",
    @ColumnInfo(name = "codigo", defaultValue = "''") val codigo: String = "",
    @ColumnInfo(name = "equipo", defaultValue = "''") val equipo: String = "",
    @ColumnInfo(name = "activo", defaultValue = "1") val activo: Boolean = true,
    @ColumnInfo(name = "id_rol") val rolId: Long
)

@Entity(tableName = "companias", indices = [Index(value = ["codigo_compania"], unique = true)])
data class CompaniaEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_compania") val id: Long = 0,
    @ColumnInfo(name = "codigo_compania") val codigo: String,
    @ColumnInfo(name = "nombre_compania") val nombre: String,
    @ColumnInfo(name = "gestiona_vehiculos") val gestionaVehiculos: Boolean,
    @ColumnInfo(name = "gestiona_hogar") val gestionaHogar: Boolean,
    @ColumnInfo(name = "plazo_dias") val plazoDias: Int,
    @ColumnInfo(name = "activa", defaultValue = "1") val activa: Boolean = true
)

@Entity(tableName = "tipos_siniestro", indices = [Index(value = ["codigo_tipo"], unique = true)])
data class TipoEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_tipo") val id: Long = 0,
    @ColumnInfo(name = "codigo_tipo") val codigo: String,
    @ColumnInfo(name = "nombre_tipo") val nombre: String,
    @ColumnInfo(name = "linea") val linea: String   // "VEHICULO" o "HOGAR"
)

@Entity(
    tableName = "siniestros",
    foreignKeys = [
        ForeignKey(entity = UsuarioEntidad::class, parentColumns = ["id_usuario"], childColumns = ["id_asegurado"]),
        ForeignKey(entity = CompaniaEntidad::class, parentColumns = ["id_compania"], childColumns = ["id_compania"]),
        ForeignKey(entity = TipoEntidad::class, parentColumns = ["id_tipo"], childColumns = ["id_tipo"]),
        ForeignKey(entity = UsuarioEntidad::class, parentColumns = ["id_usuario"], childColumns = ["id_liquidador"])
    ],
    indices = [
        Index(value = ["numero"], unique = true),
        Index(value = ["id_asegurado"]),
        Index(value = ["id_compania"]),
        Index(value = ["id_tipo"]),
        Index(value = ["id_liquidador"])
    ]
)
data class SiniestroEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_siniestro") val id: Long = 0,
    @ColumnInfo(name = "numero") val numero: String,
    @ColumnInfo(name = "id_asegurado") val aseguradoId: Long,
    @ColumnInfo(name = "id_compania") val companiaId: Long,
    @ColumnInfo(name = "id_tipo") val tipoId: Long,
    @ColumnInfo(name = "fecha_ocurrencia") val fechaOcurrencia: String,
    @ColumnInfo(name = "fecha_reporte") val fechaReporte: String,
    @ColumnInfo(name = "comuna_siniestro") val comuna: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "id_etapa", defaultValue = "1") val etapaId: Long = 1,
    @ColumnInfo(name = "fecha_cambio_etapa") val fechaCambioEtapa: String,
    @ColumnInfo(name = "id_liquidador") val liquidadorId: Long? = null,
    @ColumnInfo(name = "fuera_de_plazo", defaultValue = "0") val fueraDePlazo: Boolean = false,
    @ColumnInfo(name = "resultado") val resultado: String? = null,
    @ColumnInfo(name = "mensaje_cierre") val mensajeCierre: String? = null
)

@Entity(
    tableName = "gestiones",
    foreignKeys = [
        ForeignKey(entity = SiniestroEntidad::class, parentColumns = ["id_siniestro"], childColumns = ["id_siniestro"])
    ],
    indices = [Index(value = ["id_siniestro"])]
)
data class GestionEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_gestion") val id: Long = 0,
    @ColumnInfo(name = "id_siniestro") val siniestroId: Long,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "tipo_gestion") val tipo: String,
    @ColumnInfo(name = "descripcion_gestion") val descripcion: String,
    @ColumnInfo(name = "responsable") val responsable: String,
    @ColumnInfo(name = "visible_asegurado") val visibleAsegurado: Boolean
)

@Entity(
    tableName = "solicitudes_documento",
    foreignKeys = [
        ForeignKey(entity = SiniestroEntidad::class, parentColumns = ["id_siniestro"], childColumns = ["id_siniestro"])
    ],
    indices = [Index(value = ["id_siniestro"])]
)
data class SolicitudEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_solicitud") val id: Long = 0,
    @ColumnInfo(name = "id_siniestro") val siniestroId: Long,
    @ColumnInfo(name = "documento_solicitado") val tipoDocumento: String,
    @ColumnInfo(name = "comentario_solicitud", defaultValue = "''") val comentario: String = "",
    @ColumnInfo(name = "fecha_solicitud") val fechaSolicitud: String,
    @ColumnInfo(name = "entregado", defaultValue = "0") val entregado: Boolean = false,
    @ColumnInfo(name = "fecha_entrega") val fechaEntrega: String? = null
)

@Entity(
    tableName = "documentos_adjuntos",
    foreignKeys = [
        ForeignKey(entity = SiniestroEntidad::class, parentColumns = ["id_siniestro"], childColumns = ["id_siniestro"]),
        ForeignKey(entity = SolicitudEntidad::class, parentColumns = ["id_solicitud"], childColumns = ["id_solicitud"])
    ],
    indices = [Index(value = ["id_siniestro"]), Index(value = ["id_solicitud"])]
)
data class AdjuntoEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_adjunto") val id: Long = 0,
    @ColumnInfo(name = "id_siniestro") val siniestroId: Long,
    @ColumnInfo(name = "nombre_archivo") val nombreArchivo: String,
    @ColumnInfo(name = "ruta_archivo", defaultValue = "''") val rutaArchivo: String = "",
    @ColumnInfo(name = "tipo_documento") val tipoDocumento: String,
    @ColumnInfo(name = "fecha_carga") val fechaCarga: String,
    @ColumnInfo(name = "cargado_por") val cargadoPor: String,
    @ColumnInfo(name = "comentario", defaultValue = "''") val comentario: String = "",
    @ColumnInfo(name = "id_solicitud") val solicitudId: Long? = null
)

@Entity(
    tableName = "avisos",
    foreignKeys = [
        ForeignKey(entity = SiniestroEntidad::class, parentColumns = ["id_siniestro"], childColumns = ["id_siniestro"])
    ],
    indices = [Index(value = ["id_siniestro"])]
)
data class AvisoEntidad(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_aviso") val id: Long = 0,
    @ColumnInfo(name = "id_siniestro") val siniestroId: Long,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "motivo") val motivo: String,
    @ColumnInfo(name = "mensaje") val mensaje: String,
    @ColumnInfo(name = "leido", defaultValue = "0") val leido: Boolean = false


)