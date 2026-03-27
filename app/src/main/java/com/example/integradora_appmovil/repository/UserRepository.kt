package com.example.integradora_appmovil.repository

import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.network.MobileApiClient
import org.json.JSONObject
import java.net.URLEncoder

data class TeacherRequestRemote(
    val id: Long,
    val tipo: String,
    val area: String,
    val fecha: String,
    val estado: String,
    val puedeVerQr: Boolean
)

data class GuardFolioValidationRemote(
    val folio: String,
    val empleado: String,
    val area: String,
    val movimientoRegistrado: String,
    val fechaRegistro: String,
    val estado: String
)

class UserRepository {

    suspend fun login(correo: String, password: String): AuthSession {
        val payload = JSONObject()
            .put("correo", correo.trim())
            .put("contraseña", password)

        val response = MobileApiClient.postJson("/auth/login", payload)
        return AuthSession(
            token = response.optString("token"),
            correo = response.optString("correo"),
            nombre = response.optString("nombre"),
            rol = response.optString("rol")
        )
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String {
        val payload = JSONObject()
            .put("nombreCompleto", fullName.trim())
            .put("correo", email.trim())
            .put("contraseña", password)
            .put("confirmarContraseña", confirmPassword)

        val response = MobileApiClient.postJson("/auth/register", payload)
        return response.optString("message", "Registro completado correctamente")
    }

    suspend fun getTeacherRequests(correo: String, token: String): List<TeacherRequestRemote> {
        val encodedCorreo = URLEncoder.encode(correo, Charsets.UTF_8.name())
        val response = MobileApiClient.getJsonArray(
            path = "/solicitudes/mis-solicitudes?correo=$encodedCorreo",
            token = token
        )

        return buildList {
            for (index in 0 until response.length()) {
                val item = response.getJSONObject(index)
                add(
                    TeacherRequestRemote(
                        id = item.optLong("id"),
                        tipo = item.optString("tipo"),
                        area = item.optString("area"),
                        fecha = item.optString("fecha"),
                        estado = item.optString("estado"),
                        puedeVerQr = item.optBoolean("puedeVerQr", false)
                    )
                )
            }
        }
    }

    suspend fun validateGuardFolio(folio: String, token: String): GuardFolioValidationRemote {
        val payload = JSONObject().put("folio", folio.trim())
        val response = MobileApiClient.postJson(
            path = "/guardia/validar-folio",
            payload = payload,
            token = token
        )

        return GuardFolioValidationRemote(
            folio = response.optString("folio"),
            empleado = response.optString("empleado"),
            area = response.optString("area"),
            movimientoRegistrado = response.optString("movimientoRegistrado"),
            fechaRegistro = response.optString("fechaRegistro"),
            estado = response.optString("estado")
        )
    }
}
