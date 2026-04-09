package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class CreateAreaViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var nombreArea by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")

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
                repository.createAdminArea(nombreArea.trim(), session.token)
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
