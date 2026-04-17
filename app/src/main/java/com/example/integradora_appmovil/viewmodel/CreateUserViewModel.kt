package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.repository.AdminAreaRemote
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreateUserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    companion object {
        private const val ALLOWED_SPECIAL_CHARACTERS = "@#$%&*.,:;!?¡¿+-=/<>[](){}_~|^\\"
    }

    var nombreCompleto by mutableStateOf("")
    var correo by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var selectedRol by mutableStateOf("")
    var selectedAreaId by mutableStateOf<Long?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")
    
    var availableAreas = mutableStateOf<List<AdminAreaRemote>>(emptyList())

    fun loadAreas() {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            try {
                availableAreas.value = repository.getAdminAreas(session.token)
            } catch (e: Exception) {
                errorMessage = "Error al cargar áreas"
            }
        }
    }

    fun createUser(onSuccess: () -> Unit) {
        if (!validateForm()) return

        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val payload = JSONObject()
                    .put("nombreCompleto", nombreCompleto.trim())
                    .put("correo", correo.trim())
                    .put("contraseña", password)
                    .put("confirmarContraseña", confirmPassword)
                    .put("rol", selectedRol)
                
                selectedAreaId?.let { payload.put("areaId", it) }

                repository.createAdminUser(payload, session.token)
                successMessage = "¡Usuario creado exitosamente!"
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al crear usuario"
            } finally {
                isLoading = false
            }
        }
    }

    private fun validateForm(): Boolean {
        if (nombreCompleto.isBlank() || correo.isBlank() || password.isBlank() || selectedRol.isBlank()) {
            errorMessage = "Todos los campos son obligatorios"
            return false
        }
        if (!correo.trim().matches(Regex("^[a-zA-Z0-9]+@(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.edu\\.mx$"))) {
            errorMessage = "Ingresa un correo válido (.edu.mx)"
            return false
        }
        if (!isPasswordComplex(password)) {
            errorMessage = "La contraseña debe incluir mayúsculas, números y símbolos ($@#)"
            return false
        }
        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return false
        }
        return true
    }

    private fun isPasswordComplex(value: String): Boolean {
        val hasUppercase = value.any { it.isUpperCase() }
        val hasNumber = value.any { it.isDigit() }
        val hasSymbol = value.any { ALLOWED_SPECIAL_CHARACTERS.contains(it) }
        return value.length >= 8 && hasUppercase && hasNumber && hasSymbol
    }
}
