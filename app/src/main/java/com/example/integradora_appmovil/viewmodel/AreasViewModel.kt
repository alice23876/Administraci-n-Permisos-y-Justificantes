package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.AdminAreaRemote
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class AreasViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    val areas = mutableStateListOf<AdminAreaRemote>()
    val availableDirectors = mutableStateListOf<AdminUserRemote>()

    var showAssignDirectorDialog by mutableStateOf(false)
    var selectedArea by mutableStateOf<AdminAreaRemote?>(null)
    var successMessage by mutableStateOf("")

    fun loadAreas() {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val areasResponse = repository.getAdminAreas(session.token)
                areas.clear()
                areas.addAll(areasResponse)
                
                // Cargar posibles directores (usuarios con rol Director)
                val usersResponse = repository.getAdminUsers(session.token)
                availableDirectors.clear()
                availableDirectors.addAll(usersResponse.filter { it.rol.contains("Director", ignoreCase = true) })
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (e: Exception) {
                errorMessage = "Error al cargar áreas"
            } finally {
                isLoading = false
            }
        }
    }

    fun assignDirector(areaId: Long, directorId: Long) {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                repository.updateAdminAreaDirector(areaId, directorId, session.token)
                showAssignDirectorDialog = false
                successMessage = "¡Área asignada exitosamente!"
                loadAreas()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al asignar director"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        successMessage = ""
        errorMessage = ""
    }
}
