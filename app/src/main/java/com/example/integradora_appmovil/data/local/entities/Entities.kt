package com.example.integradora_appmovil.data.local.entities

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val activo: Boolean = true
)

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "areas",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["director_id"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("director_id")]
)
data class AreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val director_id: Long?
)

@Entity(
    tableName = "user_areas",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AreaEntity::class, parentColumns = ["id"], childColumns = ["area_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoleEntity::class, parentColumns = ["id"], childColumns = ["rol_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("user_id"), Index("area_id"), Index("rol_id")]
)
data class UserAreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user_id: Long,
    val area_id: Long,
    val rol_id: Long
)

@Entity(
    tableName = "solicitudes",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["empleado_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AreaEntity::class, parentColumns = ["id"], childColumns = ["area_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("empleado_id"), Index("area_id")]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String, // "JUSTIFICANTE" o "PERMISO"
    val empleado_id: Long,
    val area_id: Long,
    val estado: String = "PENDIENTE", // PENDIENTE, APROBADO, RECHAZADO
    val motivo: String,
    val fecha_solicitud: String
)

@Entity(
    tableName = "justificantes",
    foreignKeys = [
        ForeignKey(entity = SolicitudEntity::class, parentColumns = ["id"], childColumns = ["solicitud_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("solicitud_id")]
)
data class JustificanteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitud_id: Long,
    val fecha_incidencia: String,
    val comprobante_nombre: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val comprobante_pdf: ByteArray?
)

@Entity(
    tableName = "permisos",
    foreignKeys = [
        ForeignKey(entity = SolicitudEntity::class, parentColumns = ["id"], childColumns = ["solicitud_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("solicitud_id")]
)
data class PermisoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitud_id: Long,
    val hora_salida: String,
    val regresa_mismo_dia: Boolean = true
)
