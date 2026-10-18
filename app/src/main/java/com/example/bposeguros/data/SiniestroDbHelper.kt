package com.example.bposeguros.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bposeguros.model.Aviso
import com.example.bposeguros.model.Compania
import com.example.bposeguros.model.DocumentoAdjunto
import com.example.bposeguros.model.Etapa
import com.example.bposeguros.model.Gestion
import com.example.bposeguros.model.LineaSiniestro
import com.example.bposeguros.model.Resultado
import com.example.bposeguros.model.Rol
import com.example.bposeguros.model.Siniestro
import com.example.bposeguros.model.SolicitudDocumento
import com.example.bposeguros.model.TipoSiniestro
import com.example.bposeguros.model.Usuario

// =====================================================================
//  NUEVO (SQLite) — SiniestroDbHelper.kt
//  ---------------------------------------------------------------------
//  UNA base de datos (bposeguros.db) con NUEVE tablas relacionadas:
//
//   roles 1───N usuarios 1───N siniestros N───1 companias
//                  │               │      N───1 tipos_siniestro
//                  │               ├──1───N gestiones
//                  │               ├──1───N solicitudes_documento
//                  │               ├──1───N documentos_adjuntos
//                  │               └──1───N avisos
//                  └─ "usuarios" aparece DOS veces en siniestros:
//                     id_asegurado (quien reportó) e id_liquidador (quien lo gestiona, puede ser NULL)
//
//  Regla: UN helper por base de datos. Todas las tablas se crean en el MISMO onCreate().
//  Los datos precargados son el Anexo 9 (ficticios, S-07). Las descripciones de los siniestros
//  y algunas fechas de cambio de etapa NO vienen en el Anexo: se inventaron coherentes con él.
// =====================================================================

class SiniestroDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "bposeguros.db"
        private const val VERSION_BD = 1

        // NUEVO (Singleton): varios ViewModels comparten UN solo helper (una sola conexión)
        @Volatile private var instancia: SiniestroDbHelper? = null

        fun obtener(context: Context): SiniestroDbHelper =
            instancia ?: synchronized(this) {
                instancia ?: SiniestroDbHelper(context.applicationContext).also { instancia = it }
            }

        // ---- roles ----
        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        // ---- usuarios (asegurados, liquidadores y analistas) ----
        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_RUT = "rut"
        private const val USU_CORREO = "correo"
        private const val USU_COMUNA = "comuna"
        private const val USU_CODIGO = "codigo"
        private const val USU_EQUIPO = "equipo"
        private const val USU_ACTIVO = "activo"
        private const val USU_ROL = "id_rol"

        // ---- companias ----
        private const val TABLA_COMPANIAS = "companias"
        private const val COM_ID = "id_compania"
        private const val COM_CODIGO = "codigo_compania"
        private const val COM_NOMBRE = "nombre_compania"
        private const val COM_VEHICULOS = "gestiona_vehiculos"
        private const val COM_HOGAR = "gestiona_hogar"
        private const val COM_PLAZO = "plazo_dias"
        private const val COM_ACTIVA = "activa"

        // ---- tipos_siniestro ----
        private const val TABLA_TIPOS = "tipos_siniestro"
        private const val TIP_ID = "id_tipo"
        private const val TIP_CODIGO = "codigo_tipo"
        private const val TIP_NOMBRE = "nombre_tipo"
        private const val TIP_LINEA = "linea"

        // ---- siniestros ----
        private const val TABLA_SINIESTROS = "siniestros"
        private const val SIN_ID = "id_siniestro"
        private const val SIN_NUMERO = "numero"
        private const val SIN_ASEGURADO = "id_asegurado"
        private const val SIN_COMPANIA = "id_compania"
        private const val SIN_TIPO = "id_tipo"
        private const val SIN_F_OCURRENCIA = "fecha_ocurrencia"
        private const val SIN_F_REPORTE = "fecha_reporte"
        private const val SIN_COMUNA = "comuna_siniestro"
        private const val SIN_DESCRIPCION = "descripcion"
        private const val SIN_ETAPA = "id_etapa"
        private const val SIN_F_ETAPA = "fecha_cambio_etapa"
        private const val SIN_LIQUIDADOR = "id_liquidador"
        private const val SIN_FUERA_PLAZO = "fuera_de_plazo"
        private const val SIN_RESULTADO = "resultado"
        private const val SIN_MENSAJE = "mensaje_cierre"
        private const val ALIAS_LIQ_NOMBRE = "nombre_liquidador"
        private const val ALIAS_LIQ_EQUIPO = "equipo_liquidador"

        // ---- gestiones ----
        private const val TABLA_GESTIONES = "gestiones"
        private const val GES_ID = "id_gestion"
        private const val GES_SINIESTRO = "id_siniestro"
        private const val GES_FECHA = "fecha_hora"
        private const val GES_TIPO = "tipo_gestion"
        private const val GES_DESC = "descripcion_gestion"
        private const val GES_RESP = "responsable"
        private const val GES_VISIBLE = "visible_asegurado"

        // ---- solicitudes_documento ----
        private const val TABLA_SOLICITUDES = "solicitudes_documento"
        private const val SOL_ID = "id_solicitud"
        private const val SOL_SINIESTRO = "id_siniestro"
        private const val SOL_DOC = "documento_solicitado"
        private const val SOL_COMENTARIO = "comentario_solicitud"
        private const val SOL_F_SOL = "fecha_solicitud"
        private const val SOL_ENTREGADO = "entregado"
        private const val SOL_F_ENT = "fecha_entrega"

        // ---- documentos_adjuntos ----
        private const val TABLA_ADJUNTOS = "documentos_adjuntos"
        private const val ADJ_ID = "id_adjunto"
        private const val ADJ_SINIESTRO = "id_siniestro"
        private const val ADJ_ARCHIVO = "nombre_archivo"
        private const val ADJ_RUTA = "ruta_archivo"
        private const val ADJ_TIPO = "tipo_documento"
        private const val ADJ_FECHA = "fecha_carga"
        private const val ADJ_POR = "cargado_por"
        private const val ADJ_COMENTARIO = "comentario"
        private const val ADJ_SOLICITUD = "id_solicitud"

        // ---- avisos ----
        private const val TABLA_AVISOS = "avisos"
        private const val AVI_ID = "id_aviso"
        private const val AVI_SINIESTRO = "id_siniestro"
        private const val AVI_FECHA = "fecha_hora"
        private const val AVI_MOTIVO = "motivo"
        private const val AVI_MENSAJE = "mensaje"
        private const val AVI_LEIDO = "leido"

        // ------------------------- CREAR TABLAS -------------------------
        // Primero las tablas "padre", después las que las referencian (REFERENCES = clave foránea).

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_RUT TEXT NOT NULL DEFAULT '', " +
                    "$USU_CORREO TEXT NOT NULL DEFAULT '', " +
                    "$USU_COMUNA TEXT NOT NULL DEFAULT '', " +
                    "$USU_CODIGO TEXT NOT NULL DEFAULT '', " +
                    "$USU_EQUIPO TEXT NOT NULL DEFAULT '', " +
                    "$USU_ACTIVO INTEGER NOT NULL DEFAULT 1, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_CREAR_COMPANIAS =
            "CREATE TABLE $TABLA_COMPANIAS (" +
                    "$COM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COM_CODIGO TEXT NOT NULL UNIQUE, " +
                    "$COM_NOMBRE TEXT NOT NULL, " +
                    "$COM_VEHICULOS INTEGER NOT NULL, " +
                    "$COM_HOGAR INTEGER NOT NULL, " +
                    "$COM_PLAZO INTEGER NOT NULL, " +
                    "$COM_ACTIVA INTEGER NOT NULL DEFAULT 1)"

        private const val SQL_CREAR_TIPOS =
            "CREATE TABLE $TABLA_TIPOS (" +
                    "$TIP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TIP_CODIGO TEXT NOT NULL UNIQUE, " +
                    "$TIP_NOMBRE TEXT NOT NULL, " +
                    "$TIP_LINEA TEXT NOT NULL CHECK ($TIP_LINEA IN ('VEHICULO','HOGAR')))"

        // NUEVO (CHECK): la propia base rechaza una etapa fuera de 1..4 o un resultado inventado.
        // id_liquidador NO lleva NOT NULL: un siniestro "Recibido" aún no tiene liquidador (RN-07).
        private const val SQL_CREAR_SINIESTROS =
            "CREATE TABLE $TABLA_SINIESTROS (" +
                    "$SIN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SIN_NUMERO TEXT NOT NULL UNIQUE, " +
                    "$SIN_ASEGURADO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID), " +
                    "$SIN_COMPANIA INTEGER NOT NULL REFERENCES $TABLA_COMPANIAS($COM_ID), " +
                    "$SIN_TIPO INTEGER NOT NULL REFERENCES $TABLA_TIPOS($TIP_ID), " +
                    "$SIN_F_OCURRENCIA TEXT NOT NULL, " +
                    "$SIN_F_REPORTE TEXT NOT NULL, " +
                    "$SIN_COMUNA TEXT NOT NULL, " +
                    "$SIN_DESCRIPCION TEXT NOT NULL, " +
                    "$SIN_ETAPA INTEGER NOT NULL DEFAULT 1 CHECK ($SIN_ETAPA BETWEEN 1 AND 4), " +
                    "$SIN_F_ETAPA TEXT NOT NULL, " +
                    "$SIN_LIQUIDADOR INTEGER REFERENCES $TABLA_USUARIOS($USU_ID), " +
                    "$SIN_FUERA_PLAZO INTEGER NOT NULL DEFAULT 0, " +
                    "$SIN_RESULTADO TEXT CHECK ($SIN_RESULTADO IN ('APROBADO','RECHAZADO','DESISTIDO')), " +
                    "$SIN_MENSAJE TEXT)"

        private const val SQL_CREAR_GESTIONES =
            "CREATE TABLE $TABLA_GESTIONES (" +
                    "$GES_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$GES_SINIESTRO INTEGER NOT NULL REFERENCES $TABLA_SINIESTROS($SIN_ID), " +
                    "$GES_FECHA TEXT NOT NULL, " +
                    "$GES_TIPO TEXT NOT NULL, " +
                    "$GES_DESC TEXT NOT NULL, " +
                    "$GES_RESP TEXT NOT NULL, " +
                    "$GES_VISIBLE INTEGER NOT NULL)"

        private const val SQL_CREAR_SOLICITUDES =
            "CREATE TABLE $TABLA_SOLICITUDES (" +
                    "$SOL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SOL_SINIESTRO INTEGER NOT NULL REFERENCES $TABLA_SINIESTROS($SIN_ID), " +
                    "$SOL_DOC TEXT NOT NULL, " +
                    "$SOL_COMENTARIO TEXT NOT NULL DEFAULT '', " +
                    "$SOL_F_SOL TEXT NOT NULL, " +
                    "$SOL_ENTREGADO INTEGER NOT NULL DEFAULT 0, " +
                    "$SOL_F_ENT TEXT)"

        private const val SQL_CREAR_ADJUNTOS =
            "CREATE TABLE $TABLA_ADJUNTOS (" +
                    "$ADJ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ADJ_SINIESTRO INTEGER NOT NULL REFERENCES $TABLA_SINIESTROS($SIN_ID), " +
                    "$ADJ_ARCHIVO TEXT NOT NULL, " +
                    "$ADJ_RUTA TEXT NOT NULL DEFAULT '', " +
                    "$ADJ_TIPO TEXT NOT NULL, " +
                    "$ADJ_FECHA TEXT NOT NULL, " +
                    "$ADJ_POR TEXT NOT NULL, " +
                    "$ADJ_COMENTARIO TEXT NOT NULL DEFAULT '', " +
                    "$ADJ_SOLICITUD INTEGER REFERENCES $TABLA_SOLICITUDES($SOL_ID))"

        private const val SQL_CREAR_AVISOS =
            "CREATE TABLE $TABLA_AVISOS (" +
                    "$AVI_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$AVI_SINIESTRO INTEGER NOT NULL REFERENCES $TABLA_SINIESTROS($SIN_ID), " +
                    "$AVI_FECHA TEXT NOT NULL, " +
                    "$AVI_MOTIVO TEXT NOT NULL, " +
                    "$AVI_MENSAJE TEXT NOT NULL, " +
                    "$AVI_LEIDO INTEGER NOT NULL DEFAULT 0)"

        // ------------------------- PRECARGA (Anexo 9) -------------------------
        // Los ids van explícitos para que las claves foráneas apunten siempre al mismo registro
        // (igual que los roles del profesor). Los usuarios 1..15 coinciden con los de DatosEjemplo.

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(1, 'Asegurado'), (2, 'Liquidador'), (3, 'Analista')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_ID, $USU_NOMBRE, $USU_RUT, $USU_CORREO, $USU_COMUNA, " +
                    "$USU_CODIGO, $USU_EQUIPO, $USU_ACTIVO, $USU_ROL) VALUES " +
                    "(1, 'Camila Andrea Rojas Fuentes', '158423715', 'camila.rojas@correo.ejemplo', 'Maipú', '', '', 1, 1), " +
                    "(2, 'Jorge Luis Pizarro Contreras', '94185521', 'jorge.pizarro@correo.ejemplo', 'Puente Alto', '', '', 1, 1), " +
                    "(3, 'Valentina Soto Muñoz', '192058848', 'vale.soto@correo.ejemplo', 'Ñuñoa', '', '', 1, 1), " +
                    "(4, 'Héctor Manuel Araya Díaz', '78362197', 'h.araya@correo.ejemplo', 'Viña del Mar', '', '', 1, 1), " +
                    "(5, 'Daniela Paz Vergara Lagos', '175520937', 'daniela.vergara@correo.ejemplo', 'La Florida', '', '', 1, 1), " +
                    "(6, 'Ricardo Ignacio Espinoza Tapia', '136647083', 'r.espinoza@correo.ejemplo', 'Concepción', '', '', 1, 1), " +
                    "(7, 'Francisca Belén Morales Henríquez', '201184657', 'fran.morales@correo.ejemplo', 'Quilicura', '', '', 1, 1), " +
                    "(8, 'Luis Alberto Carrasco Peña', '112903372', 'luis.carrasco@correo.ejemplo', 'Rancagua', '', '', 1, 1), " +
                    "(9, 'Paulina Fernández Ruiz', '', '', '', 'LIQ-01', 'Vehículos', 1, 2), " +
                    "(10, 'Matías Gallardo Silva', '', '', '', 'LIQ-02', 'Hogar', 1, 2), " +
                    "(11, 'Rodrigo Salinas Ortega', '', '', '', 'LIQ-03', 'Vehículos', 1, 2), " +
                    "(12, 'Constanza Ibarra Núñez', '', '', '', 'LIQ-04', 'Hogar', 1, 2), " +
                    "(13, 'Andrés Bravo Medina', '', '', '', 'LIQ-05', 'Vehículos', 0, 2), " +
                    "(14, 'Natalia Reyes Campos', '', '', '', 'ADM-01', 'Administración', 1, 3), " +
                    "(15, 'Felipe Cortés Valdés', '', '', '', 'ADM-02', 'Administración', 1, 3)"

        private const val SQL_PRECARGAR_COMPANIAS =
            "INSERT INTO $TABLA_COMPANIAS ($COM_ID, $COM_CODIGO, $COM_NOMBRE, $COM_VEHICULOS, $COM_HOGAR, " +
                    "$COM_PLAZO, $COM_ACTIVA) VALUES " +
                    "(1, 'ASG-01', 'Seguros Cordillera', 1, 1, 10, 1), " +
                    "(2, 'ASG-02', 'Seguros Litoral', 0, 1, 12, 1), " +
                    "(3, 'ASG-03', 'Aseguradora Patagonia Sur', 1, 1, 10, 1), " +
                    "(4, 'ASG-04', 'Seguros Valle Central', 1, 0, 8, 1), " +
                    "(5, 'ASG-05', 'Protege Seguros', 0, 1, 12, 0)"

        private const val SQL_PRECARGAR_TIPOS =
            "INSERT INTO $TABLA_TIPOS ($TIP_ID, $TIP_CODIGO, $TIP_NOMBRE, $TIP_LINEA) VALUES " +
                    "(1, 'TS-01', 'Vehículo – choque o colisión', 'VEHICULO'), " +
                    "(2, 'TS-02', 'Vehículo – robo', 'VEHICULO'), " +
                    "(3, 'TS-03', 'Vehículo – rotura de cristales', 'VEHICULO'), " +
                    "(4, 'TS-04', 'Hogar – daño por agua', 'HOGAR'), " +
                    "(5, 'TS-05', 'Hogar – robo', 'HOGAR'), " +
                    "(6, 'TS-06', 'Hogar – incendio', 'HOGAR')"

        // Orden de columnas: id, número, asegurado, compañía, tipo, f.ocurrencia, f.reporte, comuna,
        // descripción, etapa, f.cambio etapa, liquidador, fuera de plazo, resultado, mensaje de cierre
        private const val SQL_PRECARGAR_SINIESTROS =
            "INSERT INTO $TABLA_SINIESTROS ($SIN_ID, $SIN_NUMERO, $SIN_ASEGURADO, $SIN_COMPANIA, $SIN_TIPO, " +
                    "$SIN_F_OCURRENCIA, $SIN_F_REPORTE, $SIN_COMUNA, $SIN_DESCRIPCION, $SIN_ETAPA, $SIN_F_ETAPA, " +
                    "$SIN_LIQUIDADOR, $SIN_FUERA_PLAZO, $SIN_RESULTADO, $SIN_MENSAJE) VALUES " +
                    "(1, 'SIN-26-004801', 1, 1, 1, '2026-09-02', '2026-09-02', 'Maipú', " +
                    "'Choque leve en un estacionamiento; se dañó el parachoques trasero.', 3, '2026-09-18', 9, 0, NULL, NULL), " +
                    "(2, 'SIN-26-004815', 2, 2, 5, '2026-08-27', '2026-08-29', 'Puente Alto', " +
                    "'Entraron a mi casa mientras no estaba y se llevaron varias especies.', 2, '2026-08-30', 10, 0, NULL, NULL), " +
                    "(3, 'SIN-26-004822', 3, 4, 3, '2026-09-10', '2026-09-10', 'Providencia', " +
                    "'Se trizó el parabrisas mientras conducía.', 4, '2026-09-21', 11, 0, 'APROBADO', " +
                    "'Tu siniestro fue aprobado por Seguros Valle Central. La compañía se contactará contigo para coordinar la reposición del parabrisas.'), " +
                    "(4, 'SIN-26-004830', 4, 3, 4, '2026-09-14', '2026-09-16', 'Viña del Mar', " +
                    "'Apareció una mancha de humedad en el cielo del living por una filtración.', 2, '2026-09-17', 12, 0, NULL, NULL), " +
                    "(5, 'SIN-26-004838', 5, 1, 2, '2026-09-20', '2026-09-21', 'La Florida', " +
                    "'Me robaron el vehículo que estaba estacionado en la calle.', 2, '2026-09-22', 9, 0, NULL, NULL), " +
                    "(6, 'SIN-26-004851', 6, 3, 6, '2026-09-25', '2026-09-26', 'Concepción', " +
                    "'Se produjo un principio de incendio en la cocina de mi casa.', 1, '2026-09-26', NULL, 0, NULL, NULL), " +
                    "(7, 'SIN-26-004857', 7, 4, 1, '2026-09-29', '2026-09-30', 'Quilicura', " +
                    "'Choque con otro vehículo en una intersección.', 1, '2026-09-30', NULL, 0, NULL, NULL), " +
                    "(8, 'SIN-26-004860', 8, 2, 4, '2026-08-10', '2026-09-28', 'Rancagua', " +
                    "'Una filtración de agua en el baño dañó el piso y una muralla.', 1, '2026-09-28', NULL, 1, NULL, NULL), " +
                    "(9, 'SIN-26-004712', 1, 1, 3, '2026-07-15', '2026-07-15', 'Santiago', " +
                    "'Se quebró un vidrio lateral del auto.', 4, '2026-07-20', 11, 0, 'DESISTIDO', " +
                    "'Cerramos tu caso porque nos indicaste que no continuarás con el reclamo. Gracias por avisarnos.'), " +
                    "(10, 'SIN-26-004745', 2, 4, 1, '2026-07-30', '2026-08-01', 'La Reina', " +
                    "'Choque en una rotonda con daños en la parte delantera.', 4, '2026-08-19', 13, 0, 'RECHAZADO', " +
                    "'Lamentamos informarte que el daño reportado no está cubierto por tu póliza. Si tienes dudas, puedes comunicarte con tu compañía de seguros.')"

        private const val SQL_PRECARGAR_GESTIONES =
            "INSERT INTO $TABLA_GESTIONES ($GES_SINIESTRO, $GES_FECHA, $GES_TIPO, $GES_DESC, $GES_RESP, $GES_VISIBLE) VALUES " +
                    "(1, '2026-09-02 18:40', 'Reporte', 'Recibimos el reporte de tu siniestro.', 'Asegurado', 1), " +
                    "(1, '2026-09-03 09:15', 'Asignación', 'Tu caso fue asignado a Paulina F. – Equipo Vehículos.', 'N. Reyes (ADM-01)', 1), " +
                    "(1, '2026-09-03 09:15', 'Cambio de etapa', 'Tu caso pasó a \"En evaluación\".', 'N. Reyes (ADM-01)', 1), " +
                    "(1, '2026-09-05 11:02', 'Solicitud de documento', 'Necesitamos el presupuesto de reparación del taller.', 'P. Fernández (LIQ-01)', 1), " +
                    "(1, '2026-09-08 16:30', 'Documento recibido', 'Recibimos tu presupuesto de reparación.', 'Asegurado', 1), " +
                    "(1, '2026-09-10 10:20', 'Gestión del liquidador', 'Se coordina inspección con taller; contacto telefónico con jefe de taller.', 'P. Fernández (LIQ-01)', 0), " +
                    "(1, '2026-09-12 10:00', 'Gestión del liquidador', 'Revisamos tu vehículo en el taller.', 'P. Fernández (LIQ-01)', 1), " +
                    "(1, '2026-09-18 15:45', 'Cambio de etapa', 'Tu caso pasó a \"En liquidación\". Estamos definiendo la resolución con tu compañía.', 'P. Fernández (LIQ-01)', 1), " +
                    "(2, '2026-08-29 10:05', 'Reporte', 'Recibimos el reporte de tu siniestro.', 'Asegurado', 1), " +
                    "(2, '2026-08-30 09:00', 'Asignación', 'Tu caso fue asignado a Matías G. – Equipo Hogar.', 'F. Cortés (ADM-02)', 1), " +
                    "(2, '2026-09-02 12:10', 'Solicitud de documento', 'Necesitamos la constancia de denuncia policial y las boletas de lo robado.', 'M. Gallardo (LIQ-02)', 1), " +
                    "(2, '2026-09-17 09:00', 'Recordatorio', 'Aún tienes documentos pendientes para continuar con tu caso.', 'Sistema', 1)"

        private const val SQL_PRECARGAR_SOLICITUDES =
            "INSERT INTO $TABLA_SOLICITUDES ($SOL_ID, $SOL_SINIESTRO, $SOL_DOC, $SOL_F_SOL, $SOL_ENTREGADO, $SOL_F_ENT) VALUES " +
                    "(1, 1, 'Presupuesto de reparación', '2026-09-05', 1, '2026-09-08'), " +
                    "(2, 2, 'Constancia de denuncia policial', '2026-09-02', 1, '2026-09-04'), " +
                    "(3, 2, 'Boletas o facturas de lo robado', '2026-09-02', 0, NULL), " +
                    "(4, 4, 'Informe o boleta del gasfíter', '2026-09-18', 0, NULL), " +
                    "(5, 5, 'Constancia de denuncia policial', '2026-09-23', 0, NULL), " +
                    "(6, 5, 'Padrón del vehículo', '2026-09-23', 0, NULL)"

        // Archivos sintéticos (S-07): no existen en el teléfono, por eso la ruta va vacía
        private const val SQL_PRECARGAR_ADJUNTOS =
            "INSERT INTO $TABLA_ADJUNTOS ($ADJ_SINIESTRO, $ADJ_ARCHIVO, $ADJ_TIPO, $ADJ_FECHA, $ADJ_POR, $ADJ_SOLICITUD) VALUES " +
                    "(1, 'foto_parachoques_1.jpg', 'Fotografía del daño', '2026-09-02', 'Asegurado', NULL), " +
                    "(1, 'foto_parachoques_2.jpg', 'Fotografía del daño', '2026-09-02', 'Asegurado', NULL), " +
                    "(1, 'presupuesto_taller.pdf', 'Presupuesto de reparación', '2026-09-08', 'Asegurado', 1), " +
                    "(2, 'constancia_denuncia.pdf', 'Constancia de denuncia policial', '2026-09-04', 'Asegurado', 2), " +
                    "(3, 'parabrisas_trizado.jpg', 'Fotografía del daño', '2026-09-10', 'Asegurado', NULL), " +
                    "(3, 'boleta_reposicion.pdf', 'Presupuesto o boleta', '2026-09-15', 'Asegurado', NULL), " +
                    "(4, 'mancha_cielo_living.jpg', 'Fotografía del daño', '2026-09-16', 'Asegurado', NULL), " +
                    "(4, 'informe_inspeccion.pdf', 'Informe de inspección', '2026-09-22', 'C. Ibarra (LIQ-04)', NULL)"

        private const val SQL_PRECARGAR_AVISOS =
            "INSERT INTO $TABLA_AVISOS ($AVI_SINIESTRO, $AVI_FECHA, $AVI_MOTIVO, $AVI_MENSAJE, $AVI_LEIDO) VALUES " +
                    "(1, '2026-09-18 15:45', 'Cambio de etapa', 'Tu siniestro SIN-26-004801 pasó a \"En liquidación\".', 1), " +
                    "(2, '2026-09-17 09:00', 'Recordatorio', 'Aún falta un documento para tu siniestro SIN-26-004815.', 0), " +
                    "(3, '2026-09-21 12:30', 'Cierre', 'Tu siniestro SIN-26-004822 fue aprobado. Revisa el mensaje de cierre.', 1), " +
                    "(4, '2026-09-18 10:05', 'Solicitud de documento', 'Necesitamos el informe o boleta del gasfíter para tu siniestro SIN-26-004830.', 1), " +
                    "(5, '2026-09-23 14:10', 'Solicitud de documento', 'Necesitamos 2 documentos para tu siniestro SIN-26-004838.', 0), " +
                    "(10, '2026-08-19 11:00', 'Cierre', 'Tu siniestro SIN-26-004745 fue cerrado. Revisa el detalle de la resolución.', 1)"

        // ------------------------- SELECT BASE (con JOIN) -------------------------
        // NUEVO (LEFT JOIN): usuarios (l) se une con LEFT porque el liquidador puede ser NULL.
        // Con INNER JOIN, los siniestros "por asignar" desaparecerían de la lista.
        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"

        private const val SQL_SELECT_SINIESTROS =
            "SELECT s.*, c.$COM_NOMBRE AS $COM_NOMBRE, t.$TIP_NOMBRE AS $TIP_NOMBRE, " +
                    "l.$USU_NOMBRE AS $ALIAS_LIQ_NOMBRE, l.$USU_EQUIPO AS $ALIAS_LIQ_EQUIPO " +
                    "FROM $TABLA_SINIESTROS s " +
                    "INNER JOIN $TABLA_COMPANIAS c ON s.$SIN_COMPANIA = c.$COM_ID " +
                    "INNER JOIN $TABLA_TIPOS t ON s.$SIN_TIPO = t.$TIP_ID " +
                    "LEFT JOIN $TABLA_USUARIOS l ON s.$SIN_LIQUIDADOR = l.$USU_ID"
    }

    // Se ejecuta CADA VEZ que se abre la conexión: SQLite trae las claves foráneas APAGADAS.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Una sola vez, cuando bposeguros.db no existe. Orden: tablas "padre" primero.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_COMPANIAS)
        db.execSQL(SQL_CREAR_TIPOS)
        db.execSQL(SQL_CREAR_SINIESTROS)
        db.execSQL(SQL_CREAR_GESTIONES)
        db.execSQL(SQL_CREAR_SOLICITUDES)
        db.execSQL(SQL_CREAR_ADJUNTOS)
        db.execSQL(SQL_CREAR_AVISOS)

        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_COMPANIAS)
        db.execSQL(SQL_PRECARGAR_TIPOS)
        db.execSQL(SQL_PRECARGAR_SINIESTROS)
        db.execSQL(SQL_PRECARGAR_GESTIONES)
        db.execSQL(SQL_PRECARGAR_SOLICITUDES)
        db.execSQL(SQL_PRECARGAR_ADJUNTOS)
        db.execSQL(SQL_PRECARGAR_AVISOS)
    }

    // Si sube VERSION_BD: borra y recrea (se pierden los datos). Orden inverso: primero las "hijas".
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_AVISOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ADJUNTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SOLICITUDES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_GESTIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SINIESTROS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_TIPOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_COMPANIAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // ---------------------------- USUARIOS (acceso simulado) ----------------------------

    // RUT ya normalizado: sin puntos ni guion (ej: 158423715). Solo busca entre asegurados.
    fun buscarAseguradoPorRut(rut: String): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.$USU_RUT = ? AND u.$USU_ROL = ?",
            arrayOf(rut, Rol.ID_ASEGURADO.toString())) { leerUsuario(it) }.firstOrNull()

    fun buscarColaboradorPorCodigo(codigo: String): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.$USU_CODIGO = ?", arrayOf(codigo)) { leerUsuario(it) }
            .firstOrNull()

    // Para recuperar la sesión guardada (SesionPreferencias guarda solo el id)
    fun buscarUsuarioPorId(id: Long): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString())) { leerUsuario(it) }
            .firstOrNull()

    // ---------------------------- CATÁLOGOS ----------------------------

    fun listarCompanias(): List<Compania> =
        consultar("SELECT * FROM $TABLA_COMPANIAS ORDER BY $COM_NOMBRE", null) { c ->
            Compania(
                id = c.largo(COM_ID),
                codigo = c.texto(COM_CODIGO),
                nombre = c.texto(COM_NOMBRE),
                lineas = buildList {
                    if (c.entero(COM_VEHICULOS) == 1) add(LineaSiniestro.VEHICULO)
                    if (c.entero(COM_HOGAR) == 1) add(LineaSiniestro.HOGAR)
                },
                plazoDiasHabiles = c.entero(COM_PLAZO),
                activa = c.entero(COM_ACTIVA) == 1
            )
        }

    fun listarTipos(): List<TipoSiniestro> =
        consultar("SELECT * FROM $TABLA_TIPOS ORDER BY $TIP_ID", null) { c ->
            TipoSiniestro(
                id = c.largo(TIP_ID),
                codigo = c.texto(TIP_CODIGO),
                nombre = c.texto(TIP_NOMBRE),
                linea = LineaSiniestro.valueOf(c.texto(TIP_LINEA))
            )
        }

    // ---------------------------- SINIESTROS ----------------------------

    // RNF-07: SIEMPRE se filtra por el asegurado. Abiertos primero y luego los más recientes (HU-02).
    // ORDER BY (id_etapa = 4): da 0 para abiertos y 1 para cerrados, así los abiertos quedan arriba.
    fun listarSiniestrosDeAsegurado(idAsegurado: Long): List<Siniestro> =
        consultar(
            "$SQL_SELECT_SINIESTROS WHERE s.$SIN_ASEGURADO = ? " +
                    "ORDER BY (s.$SIN_ETAPA = ${Etapa.CERRADO.id}), s.$SIN_F_ETAPA DESC, s.$SIN_ID DESC",
            arrayOf(idAsegurado.toString())
        ) { leerSiniestro(it) }

    // Casos del liquidador (RF-10): los abiertos más antiguos primero
    fun listarSiniestrosAsignados(idLiquidador: Long): List<Siniestro> =
        consultar(
            "$SQL_SELECT_SINIESTROS WHERE s.$SIN_LIQUIDADOR = ? " +
                    "ORDER BY (s.$SIN_ETAPA = ${Etapa.CERRADO.id}), s.$SIN_F_ETAPA, s.$SIN_ID",
            arrayOf(idLiquidador.toString())
        ) { leerSiniestro(it) }

    // RNF-07: pide también el id del asegurado, así NO se puede abrir el siniestro de otra persona
    fun buscarSiniestroDeAsegurado(idSiniestro: Long, idAsegurado: Long): Siniestro? =
        consultar(
            "$SQL_SELECT_SINIESTROS WHERE s.$SIN_ID = ? AND s.$SIN_ASEGURADO = ?",
            arrayOf(idSiniestro.toString(), idAsegurado.toString())
        ) { leerSiniestro(it) }.firstOrNull()

    // RN-06: SIN-AA-NNNNNN. Toma el correlativo más alto de ese año y suma 1 (nunca se reutiliza).
    // SUBSTR(numero, 8) = lo que va después de "SIN-26-"
    fun siguienteNumeroSiniestro(anio: Int): String {
        val aa = (anio % 100).toString().padStart(2, '0')
        var ultimo = 0
        readableDatabase.rawQuery(
            "SELECT MAX(CAST(SUBSTR($SIN_NUMERO, 8) AS INTEGER)) FROM $TABLA_SINIESTROS WHERE $SIN_NUMERO LIKE ?",
            arrayOf("SIN-$aa-%")
        ).use { cursor -> if (cursor.moveToFirst()) ultimo = cursor.getInt(0) }
        return "SIN-$aa-" + (ultimo + 1).toString().padStart(6, '0')
    }

    // Aclaración 3: misma compañía + mismo tipo + misma fecha de ocurrencia en un siniestro NO cerrado.
    // Devuelve el número del existente (o null si no hay posible duplicado).
    fun buscarPosibleDuplicado(idAsegurado: Long, idCompania: Long, idTipo: Long, fechaOcurrencia: String): String? {
        var numero: String? = null
        readableDatabase.rawQuery(
            "SELECT $SIN_NUMERO FROM $TABLA_SINIESTROS WHERE $SIN_ASEGURADO = ? AND $SIN_COMPANIA = ? " +
                    "AND $SIN_TIPO = ? AND $SIN_F_OCURRENCIA = ? AND $SIN_ETAPA <> ${Etapa.CERRADO.id} LIMIT 1",
            arrayOf(idAsegurado.toString(), idCompania.toString(), idTipo.toString(), fechaOcurrencia)
        ).use { cursor -> if (cursor.moveToFirst()) numero = cursor.getString(0) }
        return numero
    }

    // NUEVO (Transacción): guarda el siniestro Y su primera gestión ("Recibimos tu reporte", RF-12).
    // O se guardan las dos cosas o ninguna. Devuelve el id generado (o -1 si falla).
    fun insertarSiniestro(siniestro: Siniestro, fechaHora: String): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val id = db.insert(TABLA_SINIESTROS, null, valoresDe(siniestro))
            if (id == -1L) return -1L   // sin setTransactionSuccessful -> se deshace todo
            insertarGestion(
                Gestion(
                    siniestroId = id, fechaHora = fechaHora, tipo = "Reporte",
                    descripcion = "Recibimos el reporte de tu siniestro.",
                    responsable = "Asegurado", visible = true
                )
            )
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    // ---------------------------- GESTIONES, DOCUMENTOS Y AVISOS ----------------------------

    // RN-13: el asegurado pide soloVisibles = true y NUNCA recibe las internas. Orden cronológico.
    fun listarGestiones(idSiniestro: Long, soloVisibles: Boolean): List<Gestion> {
        val filtro = if (soloVisibles) " AND $GES_VISIBLE = 1" else ""
        return consultar(
            "SELECT * FROM $TABLA_GESTIONES WHERE $GES_SINIESTRO = ?$filtro ORDER BY $GES_FECHA, $GES_ID",
            arrayOf(idSiniestro.toString())
        ) { c ->
            Gestion(
                id = c.largo(GES_ID), siniestroId = c.largo(GES_SINIESTRO), fechaHora = c.texto(GES_FECHA),
                tipo = c.texto(GES_TIPO), descripcion = c.texto(GES_DESC), responsable = c.texto(GES_RESP),
                visible = c.entero(GES_VISIBLE) == 1
            )
        }
    }

    fun insertarGestion(gestion: Gestion): Long =
        writableDatabase.insert(TABLA_GESTIONES, null, ContentValues().apply {
            put(GES_SINIESTRO, gestion.siniestroId)
            put(GES_FECHA, gestion.fechaHora)
            put(GES_TIPO, gestion.tipo)
            put(GES_DESC, gestion.descripcion)
            put(GES_RESP, gestion.responsable)
            put(GES_VISIBLE, if (gestion.visible) 1 else 0)
        })

    // Pendientes primero (para que el asegurado vea lo que falta)
    fun listarSolicitudes(idSiniestro: Long): List<SolicitudDocumento> =
        consultar(
            "SELECT * FROM $TABLA_SOLICITUDES WHERE $SOL_SINIESTRO = ? ORDER BY $SOL_ENTREGADO, $SOL_F_SOL, $SOL_ID",
            arrayOf(idSiniestro.toString())
        ) { c ->
            SolicitudDocumento(
                id = c.largo(SOL_ID), siniestroId = c.largo(SOL_SINIESTRO), tipoDocumento = c.texto(SOL_DOC),
                comentario = c.texto(SOL_COMENTARIO), fechaSolicitud = c.texto(SOL_F_SOL),
                entregado = c.entero(SOL_ENTREGADO) == 1, fechaEntrega = c.textoONull(SOL_F_ENT)
            )
        }

    fun listarAdjuntos(idSiniestro: Long): List<DocumentoAdjunto> =
        consultar(
            "SELECT * FROM $TABLA_ADJUNTOS WHERE $ADJ_SINIESTRO = ? ORDER BY $ADJ_FECHA DESC, $ADJ_ID DESC",
            arrayOf(idSiniestro.toString())
        ) { c ->
            DocumentoAdjunto(
                id = c.largo(ADJ_ID), siniestroId = c.largo(ADJ_SINIESTRO), nombreArchivo = c.texto(ADJ_ARCHIVO),
                rutaArchivo = c.texto(ADJ_RUTA), tipoDocumento = c.texto(ADJ_TIPO), fechaCarga = c.texto(ADJ_FECHA),
                cargadoPor = c.texto(ADJ_POR), comentario = c.texto(ADJ_COMENTARIO),
                solicitudId = c.largoONull(ADJ_SOLICITUD)
            )
        }

    // INNER JOIN avisos + siniestros: trae el número del siniestro (Aclaración 5) y filtra por asegurado (RNF-07)
    fun listarAvisos(idAsegurado: Long): List<Aviso> =
        consultar(
            "SELECT a.*, s.$SIN_NUMERO AS $SIN_NUMERO FROM $TABLA_AVISOS a " +
                    "INNER JOIN $TABLA_SINIESTROS s ON a.$AVI_SINIESTRO = s.$SIN_ID " +
                    "WHERE s.$SIN_ASEGURADO = ? ORDER BY a.$AVI_FECHA DESC, a.$AVI_ID DESC",
            arrayOf(idAsegurado.toString())
        ) { c ->
            Aviso(
                id = c.largo(AVI_ID), siniestroId = c.largo(AVI_SINIESTRO), numeroSiniestro = c.texto(SIN_NUMERO),
                fechaHora = c.texto(AVI_FECHA), motivo = c.texto(AVI_MOTIVO), mensaje = c.texto(AVI_MENSAJE),
                leido = c.entero(AVI_LEIDO) == 1
            )
        }

    // ---------------------------- AYUDANTES ----------------------------

    // NUEVO (genérico): una sola función ejecuta cualquier SELECT y convierte cada fila con "leer".
    // El profesor tiene una por tabla (consultarUsuarios); aquí hay 6 tablas, así que se generaliza.
    private fun <T> consultar(sql: String, args: Array<String>?, leer: (Cursor) -> T): List<T> {
        val lista = mutableListOf<T>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leer(cursor))
            }
        }
        return lista
    }

    // ContentValues = mapa columna -> valor (para INSERT). Los NULL se escriben con putNull.
    private fun valoresDe(s: Siniestro) = ContentValues().apply {
        put(SIN_NUMERO, s.numero)
        put(SIN_ASEGURADO, s.aseguradoId)
        put(SIN_COMPANIA, s.companiaId)
        put(SIN_TIPO, s.tipoId)
        put(SIN_F_OCURRENCIA, s.fechaOcurrencia)
        put(SIN_F_REPORTE, s.fechaReporte)
        put(SIN_COMUNA, s.comuna)
        put(SIN_DESCRIPCION, s.descripcion)
        put(SIN_ETAPA, s.etapa.id)
        put(SIN_F_ETAPA, s.fechaCambioEtapa)
        if (s.liquidadorId != null) put(SIN_LIQUIDADOR, s.liquidadorId) else putNull(SIN_LIQUIDADOR)
        put(SIN_FUERA_PLAZO, if (s.fueraDePlazo) 1 else 0)
        if (s.resultado != null) put(SIN_RESULTADO, s.resultado.name) else putNull(SIN_RESULTADO)
        if (s.mensajeCierre != null) put(SIN_MENSAJE, s.mensajeCierre) else putNull(SIN_MENSAJE)
    }

    private fun leerUsuario(c: Cursor) = Usuario(
        id = c.largo(USU_ID),
        nombre = c.texto(USU_NOMBRE),
        rut = c.texto(USU_RUT),
        correo = c.texto(USU_CORREO),
        comuna = c.texto(USU_COMUNA),
        codigo = c.texto(USU_CODIGO),
        equipo = c.texto(USU_EQUIPO),
        activo = c.entero(USU_ACTIVO) == 1,
        rolId = c.largo(USU_ROL),
        rolNombre = c.texto(ROL_NOMBRE)      // viene de roles gracias al JOIN
    )

    // nombre_compania, nombre_tipo y los datos del liquidador vienen de los JOIN
    private fun leerSiniestro(c: Cursor) = Siniestro(
        id = c.largo(SIN_ID),
        numero = c.texto(SIN_NUMERO),
        aseguradoId = c.largo(SIN_ASEGURADO),
        companiaId = c.largo(SIN_COMPANIA),
        companiaNombre = c.texto(COM_NOMBRE),
        tipoId = c.largo(SIN_TIPO),
        tipoNombre = c.texto(TIP_NOMBRE),
        fechaOcurrencia = c.texto(SIN_F_OCURRENCIA),
        fechaReporte = c.texto(SIN_F_REPORTE),
        comuna = c.texto(SIN_COMUNA),
        descripcion = c.texto(SIN_DESCRIPCION),
        etapa = Etapa.desdeId(c.largo(SIN_ETAPA)),
        fechaCambioEtapa = c.texto(SIN_F_ETAPA),
        liquidadorId = c.largoONull(SIN_LIQUIDADOR),
        liquidadorNombre = c.textoONull(ALIAS_LIQ_NOMBRE),
        liquidadorEquipo = c.textoONull(ALIAS_LIQ_EQUIPO),
        fueraDePlazo = c.entero(SIN_FUERA_PLAZO) == 1,
        resultado = c.textoONull(SIN_RESULTADO)?.let { Resultado.valueOf(it) },
        mensajeCierre = c.textoONull(SIN_MENSAJE)
    )

    // Lectores cortos del Cursor (evitan repetir getColumnIndexOrThrow en cada columna)
    private fun Cursor.texto(col: String): String = getString(getColumnIndexOrThrow(col))
    private fun Cursor.largo(col: String): Long = getLong(getColumnIndexOrThrow(col))
    private fun Cursor.entero(col: String): Int = getInt(getColumnIndexOrThrow(col))
    private fun Cursor.textoONull(col: String): String? =
        getColumnIndexOrThrow(col).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.largoONull(col: String): Long? =
        getColumnIndexOrThrow(col).let { if (isNull(it)) null else getLong(it) }
}