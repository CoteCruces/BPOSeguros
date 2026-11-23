package com.example.bposeguros.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [
        ComunaEntidad::class,
        RolEntidad::class,
        UsuarioEntidad::class,
        CompaniaEntidad::class,
        TipoEntidad::class,
        SiniestroEntidad::class,
        GestionEntidad::class,
        SolicitudEntidad::class,
        AdjuntoEntidad::class,
        AvisoEntidad::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BpoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun siniestroDao(): SiniestroDao
    abstract fun seguimientoDao(): SeguimientoDao

    companion object {
        private const val NOMBRE = "bposeguros_room.db"
        private const val NOMBRE_ANTIGUO = "bposeguros.db"

        @Volatile
        private var instancia: BpoDatabase? = null

        fun obtener(contexto: Context): BpoDatabase =
            instancia ?: synchronized(this) {
                instancia ?: construir(contexto.applicationContext).also { instancia = it }
            }

        private fun construir(contexto: Context): BpoDatabase {

            contexto.deleteDatabase(NOMBRE_ANTIGUO)

            return Room.databaseBuilder(contexto, BpoDatabase::class.java, NOMBRE)
                .addCallback(object : RoomDatabase.Callback() {

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Semillas.cargar(db)
                    }
                })
                .build()
        }
    }
}