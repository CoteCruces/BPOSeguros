package com.example.bposeguros.data.local


object Consultas {
    const val SELECT_SINIESTROS =
        "SELECT s.*, c.nombre_compania AS nombre_compania, t.nombre_tipo AS nombre_tipo, " +
                "l.nombre AS nombre_liquidador, l.equipo AS equipo_liquidador, " +
                "a.nombre AS nombre_asegurado, a.rut AS rut_asegurado, " +
                "(SELECT MAX(g.fecha_hora) FROM gestiones g " +
                "WHERE g.id_siniestro = s.id_siniestro AND g.visible_asegurado = 1) AS ultima_actualizacion " +
                "FROM siniestros s " +
                "INNER JOIN companias c ON s.id_compania = c.id_compania " +
                "INNER JOIN tipos_siniestro t ON s.id_tipo = t.id_tipo " +
                "INNER JOIN usuarios a ON s.id_asegurado = a.id_usuario " +
                "LEFT JOIN usuarios l ON s.id_liquidador = l.id_usuario"
}