package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class CreateAreaViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var nombreArea by mutableStateOf("")
    var selectedDirectorId by mutableStateOf<Long?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")

    val availableDirectors = mutableStateListOf<AdminUserRemote>()

    fun loadDirectors() {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            try {
                val users = repository.getAdminUsers(session.token)
                availableDirectors.clear()
                availableDirectors.addAll(users.filter { it.rol.contains("Director", ignoreCase = true) })
            } catch (e: Exception) {
                errorMessage = "Error al cargar directores"
            }
        }
    }

    fun createArea(onSuccess: () -> Unit) {
        if (nombreArea.isBlank()) {
            errorMessage = "El nombre del área es obligatorio"
            return
        }

        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                repository.createAdminArea(nombreArea.trim(), selectedDirectorId, session.token)
                successMessage = "¡Área registrada exitosamente!"
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al crear área"
            } finally {
                isLoading = false
            }
        }
    }
}
