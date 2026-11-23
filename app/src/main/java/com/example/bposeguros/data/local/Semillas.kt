package com.example.bposeguros.data.local

import androidx.sqlite.db.SupportSQLiteDatabase


object Semillas {

    fun cargar(db: SupportSQLiteDatabase) {
        db.execSQL(COMUNAS)
        db.execSQL(ROLES)
        db.execSQL(USUARIOS)
        db.execSQL(COMPANIAS)
        db.execSQL(TIPOS)
        db.execSQL(SINIESTROS)
        db.execSQL(GESTIONES)
        db.execSQL(SOLICITUDES)
        db.execSQL(ADJUNTOS)
        db.execSQL(AVISOS)
    }

    private const val COMUNAS = """
INSERT INTO comunas (nombre_comuna) VALUES
    ('Cerrillos'),
    ('Cerro Navia'),
    ('Colina'),
    ('Concepción'),
    ('Conchalí'),
    ('El Bosque'),
    ('Estación Central'),
    ('Huechuraba'),
    ('Independencia'),
    ('La Cisterna'),
    ('La Florida'),
    ('La Granja'),
    ('La Pintana'),
    ('La Reina'),
    ('Las Condes'),
    ('Lo Barnechea'),
    ('Lo Espejo'),
    ('Lo Prado'),
    ('Macul'),
    ('Maipú'),
    ('Ñuñoa'),
    ('Pedro Aguirre Cerda'),
    ('Peñalolén'),
    ('Providencia'),
    ('Pudahuel'),
    ('Puente Alto'),
    ('Quilicura'),
    ('Quinta Normal'),
    ('Rancagua'),
    ('Recoleta'),
    ('Renca'),
    ('San Bernardo'),
    ('San Joaquín'),
    ('San Miguel'),
    ('San Ramón'),
    ('Santiago'),
    ('Valparaíso'),
    ('Viña del Mar'),
    ('Vitacura')
"""

    private const val ROLES = """
INSERT INTO roles (id_rol, nombre_rol) VALUES
    (1, 'Asegurado'),
    (2, 'Liquidador'),
    (3, 'Analista')
"""

    private const val USUARIOS = """
INSERT INTO usuarios (id_usuario, nombre, rut, correo, comuna, codigo, equipo, activo, id_rol) VALUES
    (1, 'Camila Andrea Rojas Fuentes', '158423715', 'camila.rojas@correo.ejemplo', 'Maipú', '', '', 1, 1),
    (2, 'Jorge Luis Pizarro Contreras', '94185521', 'jorge.pizarro@correo.ejemplo', 'Puente Alto', '', '', 1, 1),
    (3, 'Valentina Soto Muñoz', '192058848', 'vale.soto@correo.ejemplo', 'Ñuñoa', '', '', 1, 1),
    (4, 'Héctor Manuel Araya Díaz', '78362197', 'h.araya@correo.ejemplo', 'Viña del Mar', '', '', 1, 1),
    (5, 'Daniela Paz Vergara Lagos', '175520937', 'daniela.vergara@correo.ejemplo', 'La Florida', '', '', 1, 1),
    (6, 'Ricardo Ignacio Espinoza Tapia', '136647083', 'r.espinoza@correo.ejemplo', 'Concepción', '', '', 1, 1),
    (7, 'Francisca Belén Morales Henríquez', '201184657', 'fran.morales@correo.ejemplo', 'Quilicura', '', '', 1, 1),
    (8, 'Luis Alberto Carrasco Peña', '112903372', 'luis.carrasco@correo.ejemplo', 'Rancagua', '', '', 1, 1),
    (9, 'Paulina Fernández Ruiz', '162345672', 'paulina.fernandez@andes.ejemplo', '', 'LIQ-01', 'Vehículos', 1, 2),
    (10, 'Matías Gallardo Silva', '138765431', 'matias.gallardo@andes.ejemplo', '', 'LIQ-02', 'Hogar', 1, 2),
    (11, 'Rodrigo Salinas Ortega', '183459120', 'rodrigo.salinas@andes.ejemplo', '', 'LIQ-03', 'Vehículos', 1, 2),
    (12, 'Constanza Ibarra Núñez', '145678900', 'constanza.ibarra@andes.ejemplo', '', 'LIQ-04', 'Hogar', 1, 2),
    (13, 'Andrés Bravo Medina', '11223344K', 'andres.bravo@andes.ejemplo', '', 'LIQ-05', 'Vehículos', 0, 2),
    (14, 'Natalia Reyes Campos', '198761230', 'natalia.reyes@andes.ejemplo', '', 'ADM-01', 'Administración', 1, 3),
    (15, 'Felipe Cortés Valdés', '109876542', 'felipe.cortes@andes.ejemplo', '', 'ADM-02', 'Administración', 1, 3)
"""

    private const val COMPANIAS = """
INSERT INTO companias (id_compania, codigo_compania, nombre_compania, gestiona_vehiculos, gestiona_hogar, plazo_dias, activa) VALUES
    (1, 'ASG-01', 'Seguros Cordillera', 1, 1, 10, 1),
    (2, 'ASG-02', 'Seguros Litoral', 0, 1, 12, 1),
    (3, 'ASG-03', 'Aseguradora Patagonia Sur', 1, 1, 10, 1),
    (4, 'ASG-04', 'Seguros Valle Central', 1, 0, 8, 1),
    (5, 'ASG-05', 'Protege Seguros', 0, 1, 12, 0)
"""

    private const val TIPOS = """
INSERT INTO tipos_siniestro (id_tipo, codigo_tipo, nombre_tipo, linea) VALUES
    (1, 'TS-01', 'Vehículo – choque o colisión', 'VEHICULO'),
    (2, 'TS-02', 'Vehículo – robo', 'VEHICULO'),
    (3, 'TS-03', 'Vehículo – rotura de cristales', 'VEHICULO'),
    (4, 'TS-04', 'Hogar – daño por agua', 'HOGAR'),
    (5, 'TS-05', 'Hogar – robo', 'HOGAR'),
    (6, 'TS-06', 'Hogar – incendio', 'HOGAR')
"""

    private const val SINIESTROS = """
INSERT INTO siniestros (id_siniestro, numero, id_asegurado, id_compania, id_tipo, fecha_ocurrencia, fecha_reporte, comuna_siniestro, descripcion, id_etapa, fecha_cambio_etapa, id_liquidador, fuera_de_plazo, resultado, mensaje_cierre) VALUES
    (1, 'SIN-26-004801', 1, 1, 1, '2026-09-02', '2026-09-02', 'Maipú', 'Choque leve en un estacionamiento; se dañó el parachoques trasero.', 3, '2026-09-18', 9, 0, NULL, NULL),
    (2, 'SIN-26-004815', 2, 2, 5, '2026-08-27', '2026-08-29', 'Puente Alto', 'Entraron a mi casa mientras no estaba y se llevaron varias especies.', 2, '2026-08-30', 10, 0, NULL, NULL),
    (3, 'SIN-26-004822', 3, 4, 3, '2026-09-10', '2026-09-10', 'Providencia', 'Se trizó el parabrisas mientras conducía.', 4, '2026-09-21', 11, 0, 'APROBADO', 'Tu siniestro fue aprobado por Seguros Valle Central. La compañía se contactará contigo para coordinar la reposición del parabrisas.'),
    (4, 'SIN-26-004830', 4, 3, 4, '2026-09-14', '2026-09-16', 'Viña del Mar', 'Apareció una mancha de humedad en el cielo del living por una filtración.', 2, '2026-09-17', 12, 0, NULL, NULL),
    (5, 'SIN-26-004838', 5, 1, 2, '2026-09-20', '2026-09-21', 'La Florida', 'Me robaron el vehículo que estaba estacionado en la calle.', 2, '2026-09-22', 9, 0, NULL, NULL),
    (6, 'SIN-26-004851', 6, 3, 6, '2026-09-25', '2026-09-26', 'Concepción', 'Se produjo un principio de incendio en la cocina de mi casa.', 1, '2026-09-26', NULL, 0, NULL, NULL),
    (7, 'SIN-26-004857', 7, 4, 1, '2026-09-29', '2026-09-30', 'Quilicura', 'Choque con otro vehículo en una intersección.', 1, '2026-09-30', NULL, 0, NULL, NULL),
    (8, 'SIN-26-004860', 8, 2, 4, '2026-08-10', '2026-09-28', 'Rancagua', 'Una filtración de agua en el baño dañó el piso y una muralla.', 1, '2026-09-28', NULL, 1, NULL, NULL),
    (9, 'SIN-26-004712', 1, 1, 3, '2026-07-15', '2026-07-15', 'Santiago', 'Se quebró un vidrio lateral del auto.', 4, '2026-07-20', 11, 0, 'DESISTIDO', 'Cerramos tu caso porque nos indicaste que no continuarás con el reclamo. Gracias por avisarnos.'),
    (10, 'SIN-26-004745', 2, 4, 1, '2026-07-30', '2026-08-01', 'La Reina', 'Choque en una rotonda con daños en la parte delantera.', 4, '2026-08-19', 13, 0, 'RECHAZADO', 'Lamentamos informarte que el daño reportado no está cubierto por tu póliza. Si tienes dudas, puedes comunicarte con tu compañía de seguros.')
"""

    private const val GESTIONES = """
INSERT INTO gestiones (id_siniestro, fecha_hora, tipo_gestion, descripcion_gestion, responsable, visible_asegurado) VALUES
    (1, '2026-09-02 18:40', 'Reporte', 'Recibimos el reporte de tu siniestro.', 'Asegurado', 1),
    (1, '2026-09-03 09:15', 'Asignación', 'Tu caso fue asignado a Paulina F. – Equipo Vehículos.', 'N. Reyes (ADM-01)', 1),
    (1, '2026-09-03 09:15', 'Cambio de etapa', 'Tu caso pasó a "En evaluación".', 'N. Reyes (ADM-01)', 1),
    (1, '2026-09-05 11:02', 'Solicitud de documento', 'Necesitamos el presupuesto de reparación del taller.', 'P. Fernández (LIQ-01)', 1),
    (1, '2026-09-08 16:30', 'Documento recibido', 'Recibimos tu presupuesto de reparación.', 'Asegurado', 1),
    (1, '2026-09-10 10:20', 'Gestión del liquidador', 'Se coordina inspección con taller; contacto telefónico con jefe de taller.', 'P. Fernández (LIQ-01)', 0),
    (1, '2026-09-12 10:00', 'Gestión del liquidador', 'Revisamos tu vehículo en el taller.', 'P. Fernández (LIQ-01)', 1),
    (1, '2026-09-18 15:45', 'Cambio de etapa', 'Tu caso pasó a "En liquidación". Estamos definiendo la resolución con tu compañía.', 'P. Fernández (LIQ-01)', 1),
    (2, '2026-08-29 10:05', 'Reporte', 'Recibimos el reporte de tu siniestro.', 'Asegurado', 1),
    (2, '2026-08-30 09:00', 'Asignación', 'Tu caso fue asignado a Matías G. – Equipo Hogar.', 'F. Cortés (ADM-02)', 1),
    (2, '2026-09-02 12:10', 'Solicitud de documento', 'Necesitamos la constancia de denuncia policial y las boletas de lo robado.', 'M. Gallardo (LIQ-02)', 1),
    (2, '2026-09-17 09:00', 'Recordatorio', 'Aún tienes documentos pendientes para continuar con tu caso.', 'Sistema', 1)
"""

    private const val SOLICITUDES = """
INSERT INTO solicitudes_documento (id_solicitud, id_siniestro, documento_solicitado, fecha_solicitud, entregado, fecha_entrega) VALUES
    (1, 1, 'Presupuesto de reparación', '2026-09-05', 1, '2026-09-08'),
    (2, 2, 'Constancia de denuncia policial', '2026-09-02', 1, '2026-09-04'),
    (3, 2, 'Boletas o facturas de lo robado', '2026-09-02', 0, NULL),
    (4, 4, 'Informe o boleta del gasfíter', '2026-09-18', 0, NULL),
    (5, 5, 'Constancia de denuncia policial', '2026-09-23', 0, NULL),
    (6, 5, 'Padrón del vehículo', '2026-09-23', 0, NULL)
"""

    private const val ADJUNTOS = """
INSERT INTO documentos_adjuntos (id_siniestro, nombre_archivo, tipo_documento, fecha_carga, cargado_por, id_solicitud) VALUES
    (1, 'foto_parachoques_1.jpg', 'Fotografía del daño', '2026-09-02', 'Asegurado', NULL),
    (1, 'foto_parachoques_2.jpg', 'Fotografía del daño', '2026-09-02', 'Asegurado', NULL),
    (1, 'presupuesto_taller.pdf', 'Presupuesto de reparación', '2026-09-08', 'Asegurado', 1),
    (2, 'constancia_denuncia.pdf', 'Constancia de denuncia policial', '2026-09-04', 'Asegurado', 2),
    (3, 'parabrisas_trizado.jpg', 'Fotografía del daño', '2026-09-10', 'Asegurado', NULL),
    (3, 'boleta_reposicion.pdf', 'Presupuesto o boleta', '2026-09-15', 'Asegurado', NULL),
    (4, 'mancha_cielo_living.jpg', 'Fotografía del daño', '2026-09-16', 'Asegurado', NULL),
    (4, 'informe_inspeccion.pdf', 'Informe de inspección', '2026-09-22', 'C. Ibarra (LIQ-04)', NULL)
"""

    private const val AVISOS = """
INSERT INTO avisos (id_siniestro, fecha_hora, motivo, mensaje, leido) VALUES
    (1, '2026-09-18 15:45', 'Cambio de etapa', 'Tu siniestro SIN-26-004801 pasó a "En liquidación".', 1),
    (2, '2026-09-17 09:00', 'Recordatorio', 'Aún falta un documento para tu siniestro SIN-26-004815.', 0),
    (3, '2026-09-21 12:30', 'Cierre', 'Tu siniestro SIN-26-004822 fue aprobado. Revisa el mensaje de cierre.', 1),
    (4, '2026-09-18 10:05', 'Solicitud de documento', 'Necesitamos el informe o boleta del gasfíter para tu siniestro SIN-26-004830.', 1),
    (5, '2026-09-23 14:10', 'Solicitud de documento', 'Necesitamos 2 documentos para tu siniestro SIN-26-004838.', 0),
    (10, '2026-08-19 11:00', 'Cierre', 'Tu siniestro SIN-26-004745 fue cerrado. Revisa el detalle de la resolución.', 1)
"""

}