package com.example.integradora_appmovil.data.local.dao

import androidx.room.*
import com.example.integradora_appmovil.data.local.entities.*

@Dao
interface AppDao {
    @Query("SELECT * FROM users WHERE correo = :email AND password = :pass LIMIT 1")
    suspend fun login(email: String, pass: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserArea(userArea: UserAreaEntity)

    @Query("""
        SELECT roles.nombre FROM roles 
        INNER JOIN user_areas ON roles.id = user_areas.rol_id 
        WHERE user_areas.user_id = :userId LIMIT 1
    """)
    suspend fun getUserRole(userId: Long): String?

    @Query("SELECT * FROM solicitudes WHERE empleado_id = :userId")
    suspend fun getSolicitudesByUser(userId: Long): List<SolicitudEntity>

    @Insert
    suspend fun insertSolicitud(solicitud: SolicitudEntity)
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?
}
