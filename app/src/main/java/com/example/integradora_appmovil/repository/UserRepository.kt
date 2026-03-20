package com.example.integradora_appmovil.repository

import com.example.integradora_appmovil.model.User
import kotlinx.coroutines.delay

class UserRepository {
    companion object {
        private val registeredUsers = mutableListOf<Triple<String, String, String>>(
            // Usuario, password, email
            Triple("admin", "123456", "@.edu.mx")
        )
        fun addUser(username: String, password: String, email: String) {
            registeredUsers.add(Triple(username, password, email))
        }
        fun findUser(username: String, password: String): Boolean {
            return registeredUsers.any { it.first == username && it.second == password }
        }
    }

    suspend fun authenticate(username: String, password: String): Int? {
        delay(1500) // Simular latencia de red
        
        return if (findUser(username, password)) {
            1 // ID de usuario simulado
        } else {
            null
        }
    }

    suspend fun register(username: String, password: String, email: String): Boolean {
        delay(1000)
        addUser(username, password, email)
        return true
    }
}
