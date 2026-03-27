package com.example.integradora_appmovil.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AuthSession(
    val token: String,
    val correo: String,
    val nombre: String,
    val rol: String
)

object SessionManager {
    var currentUser by mutableStateOf<AuthSession?>(null)
        private set

    fun startSession(session: AuthSession) {
        currentUser = session
    }

    fun clearSession() {
        currentUser = null
    }
}
