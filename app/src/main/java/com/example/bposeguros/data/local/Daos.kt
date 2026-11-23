package com.example.bposeguros.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface UsuarioDao {

    @Query("SELECT u.*, r.nombre_rol AS nombre_rol FROM usuarios u INNER JOIN roles r ON u.id_rol = r.id_rol WHERE u.id_usuario = :id")
    suspend fun buscarPorId(id: Long): UsuarioFila?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id")
    suspend fun entidad(id: Long): UsuarioEntidad?


    @Upsert
    suspend fun guardar(usuario: UsuarioEntidad)
}

@Dao
interface CatalogoDao {

    @Query("SELECT nombre_comuna FROM comunas ORDER BY id_comuna")
    suspend fun comunas(): List<String>

    @Query("SELECT * FROM companias ORDER BY nombre_compania")
    suspend fun companias(): List<CompaniaEntidad>

    @Query("SELECT * FROM tipos_siniestro ORDER BY id_tipo")
    suspend fun tipos(): List<TipoEntidad>
}

@Dao
interface SiniestroDao {

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_asegurado = :idAsegurado ORDER BY (s.id_etapa = 4), s.fecha_cambio_etapa DESC, s.id_siniestro DESC")
    suspend fun deAsegurado(idAsegurado: Long): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_asegurado = :idAsegurado ORDER BY s.fecha_reporte DESC, s.id_siniestro DESC LIMIT :cantidad")
    suspend fun ultimos(idAsegurado: Long, cantidad: Int): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_asegurado = :idAsegurado AND s.fecha_ocurrencia BETWEEN :desde AND :hasta " +
            "ORDER BY s.fecha_ocurrencia DESC, s.id_siniestro DESC")
    suspend fun entreFechas(idAsegurado: Long, desde: String, hasta: String): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_liquidador = :idLiquidador ORDER BY (s.id_etapa = 4), s.fecha_cambio_etapa, s.id_siniestro")
    suspend fun asignados(idLiquidador: Long): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_liquidador IS NULL AND s.id_etapa <> 4 ORDER BY s.id_siniestro")
    suspend fun sinAsignar(): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS +
            " WHERE s.id_liquidador = :idLiquidador AND (s.numero LIKE :texto OR a.rut LIKE :rut OR a.nombre LIKE :texto) " +
            "ORDER BY (s.id_etapa = 4), s.fecha_cambio_etapa, s.id_siniestro")
    suspend fun buscarCasos(idLiquidador: Long, texto: String, rut: String): List<SiniestroFila>

    @Query(Consultas.SELECT_SINIESTROS + " WHERE s.id_siniestro = :idSiniestro AND s.id_asegurado = :idAsegurado")
    suspend fun deAseguradoPorId(idSiniestro: Long, idAsegurado: Long): SiniestroFila?

    @Query(Consultas.SELECT_SINIESTROS + " WHERE s.id_siniestro = :idSiniestro AND s.id_liquidador = :idLiquidador")
    suspend fun deLiquidadorPorId(idSiniestro: Long, idLiquidador: Long): SiniestroFila?

    @Insert
    suspend fun insertar(siniestro: SiniestroEntidad): Long

    @Query("UPDATE siniestros SET id_etapa = :etapa, fecha_cambio_etapa = :fecha WHERE id_siniestro = :id")
    suspend fun actualizarEtapa(id: Long, etapa: Long, fecha: String): Int

    @Query("UPDATE siniestros SET id_etapa = 4, fecha_cambio_etapa = :fecha, resultado = :resultado, mensaje_cierre = :mensaje WHERE id_siniestro = :id")
    suspend fun cerrar(id: Long, resultado: String, mensaje: String, fecha: String): Int


    @Query("UPDATE siniestros SET id_liquidador = :idLiquidador, id_etapa = 2, fecha_cambio_etapa = :fecha WHERE id_siniestro = :id AND id_liquidador IS NULL")
    suspend fun asignar(id: Long, idLiquidador: Long, fecha: String): Int

    @Query("SELECT MAX(CAST(SUBSTR(numero, 8) AS INTEGER)) FROM siniestros WHERE numero LIKE :patron")
    suspend fun ultimoNumero(patron: String): Int?

    @Query("SELECT numero FROM siniestros WHERE id_asegurado = :idAsegurado AND id_compania = :idCompania AND id_tipo = :idTipo AND fecha_ocurrencia = :fecha AND id_etapa <> 4 LIMIT 1")
    suspend fun duplicado(idAsegurado: Long, idCompania: Long, idTipo: Long, fecha: String): String?

    @Query("SELECT t.linea FROM siniestros s INNER JOIN tipos_siniestro t ON s.id_tipo = t.id_tipo WHERE s.id_siniestro = :id")
    suspend fun lineaDe(id: Long): String?

    @Query("SELECT u.id_usuario, u.nombre, u.codigo, u.equipo, " +
            "(SELECT COUNT(*) FROM siniestros x WHERE x.id_liquidador = u.id_usuario AND x.id_etapa <> 4) AS abiertos " +
            "FROM usuarios u WHERE u.id_rol = 2 AND u.activo = 1 AND u.equipo = :equipo ORDER BY abiertos, u.id_usuario")
    suspend fun candidatos(equipo: String): List<CandidatoFila>
}

@Dao
interface SeguimientoDao {


    @Query("SELECT * FROM gestiones WHERE id_siniestro = :id ORDER BY fecha_hora, id_gestion")
    suspend fun gestiones(id: Long): List<GestionEntidad>


    @Query("SELECT * FROM gestiones WHERE id_siniestro = :id AND visible_asegurado = 1 AND tipo_gestion <> :tipoNota ORDER BY fecha_hora, id_gestion")
    suspend fun gestionesVisibles(id: Long, tipoNota: String): List<GestionEntidad>

    @Insert
    suspend fun insertarGestion(gestion: GestionEntidad): Long


    @Query("SELECT * FROM solicitudes_documento WHERE id_siniestro = :id ORDER BY entregado, fecha_solicitud, id_solicitud")
    suspend fun solicitudes(id: Long): List<SolicitudEntidad>

    @Insert
    suspend fun insertarSolicitud(solicitud: SolicitudEntidad): Long

    @Query("UPDATE solicitudes_documento SET entregado = 1, fecha_entrega = :fecha WHERE id_solicitud = :id")
    suspend fun marcarEntregada(id: Long, fecha: String): Int

    @Query("SELECT COUNT(*) FROM solicitudes_documento WHERE id_siniestro = :id AND entregado = 0")
    suspend fun contarPendientes(id: Long): Int

    @Query("SELECT COUNT(*) FROM solicitudes_documento WHERE id_siniestro = :id AND documento_solicitado = :documento AND entregado = 0")
    suspend fun contarPendientesDe(id: Long, documento: String): Int


    @Query("SELECT * FROM documentos_adjuntos WHERE id_siniestro = :id ORDER BY fecha_carga DESC, id_adjunto DESC")
    suspend fun adjuntos(id: Long): List<AdjuntoEntidad>

    @Query("SELECT COUNT(*) FROM documentos_adjuntos WHERE id_siniestro = :id")
    suspend fun contarAdjuntos(id: Long): Int

    @Insert
    suspend fun insertarAdjunto(adjunto: AdjuntoEntidad): Long


    @Query("SELECT a.*, s.numero AS numero FROM avisos a INNER JOIN siniestros s ON a.id_siniestro = s.id_siniestro " +
            "WHERE s.id_asegurado = :idAsegurado ORDER BY a.fecha_hora DESC, a.id_aviso DESC")
    suspend fun avisos(idAsegurado: Long): List<AvisoFila>

    @Insert
    suspend fun insertarAviso(aviso: AvisoEntidad): Long

    @Query("UPDATE avisos SET leido = 1 WHERE id_aviso = :id")
    suspend fun marcarAvisoLeido(id: Long): Int
}