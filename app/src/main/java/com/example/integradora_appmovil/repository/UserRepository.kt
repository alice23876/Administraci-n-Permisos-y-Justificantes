package com.example.integradora_appmovil.repository

import com.example.integradora_appmovil.model.User
import kotlinx.coroutines.delay

class UserRepository {
    suspend fun authenticate(username: String, password: String): Int? {
        delay(1500) // Simular latencia de red

        return if (username == "admin" && password == "123456") {
            1
        } else {
            null
        }
    }
}