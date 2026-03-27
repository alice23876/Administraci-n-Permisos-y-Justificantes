package com.example.integradora_appmovil.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.integradora_appmovil.data.local.dao.AppDao
import com.example.integradora_appmovil.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class, 
        RoleEntity::class, 
        AreaEntity::class, 
        UserAreaEntity::class, 
        SolicitudEntity::class, 
        JustificanteEntity::class, 
        PermisoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "permisos_utez_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.appDao()
                        
                        // 1. Insertar Roles
                        dao.insertRole(RoleEntity(1, "Administrador"))
                        dao.insertRole(RoleEntity(2, "Director de area"))
                        dao.insertRole(RoleEntity(3, "Docente"))
                        dao.insertRole(RoleEntity(4, "Guardia"))

                        // 2. Insertar Usuarios (Password: 123456 para todos)
                        dao.insertUser(UserEntity(1, "Admin Sistema", "admin@utez.edu.mx", "1234_56"))
                        dao.insertUser(UserEntity(2, "Roberto Director", "director@utez.edu.mx", "1234_56"))
                        dao.insertUser(UserEntity(3, "Elena Pérez", "elena@utez.edu.mx", "123456"))
                        dao.insertUser(UserEntity(4, "Juan Guardia", "guardia@utez.edu.mx", "1234_56"))

                        // 3. Insertar Áreas
                        dao.insertArea(AreaEntity(1, "DATIC", 2))
                        dao.insertArea(AreaEntity(2, "Recursos Humanos", null))

                        // 4. Vincular Usuarios con Áreas y Roles
                        dao.insertUserArea(UserAreaEntity(1, 1, 1, 1)) // Admin en DATIC
                        dao.insertUserArea(UserAreaEntity(2, 2, 1, 2)) // Roberto Director en DATIC
                        dao.insertUserArea(UserAreaEntity(3, 3, 1, 3)) // Elena Docente en DATIC
                        dao.insertUserArea(UserAreaEntity(4, 4, 2, 4)) // Juan Guardia en RRHH
                    }
                }
            }
        }
    }
}
