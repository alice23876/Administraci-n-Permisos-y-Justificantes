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

class UsersViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var searchTerm by mutableStateOf("")

    val users = mutableStateListOf<AdminUserRemote>()
    val areas = mutableStateListOf<AdminAreaRemote>()

    var showRoleDialog by mutableStateOf(false)
    var showAreaDialog by mutableStateOf(false)
    var selectedUser by mutableStateOf<AdminUserRemote?>(null)
    var successMessage by mutableStateOf("")

    fun loadData() {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val usersResponse = repository.getAdminUsers(session.token)
                val areasResponse = repository.getAdminAreas(session.token)
                users.clear()
                users.addAll(usersResponse)
                areas.clear()
                areas.addAll(areasResponse)
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (e: Exception) {
                errorMessage = "Error al cargar datos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateRole(userId: Long, newRole: String) {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                repository.updateAdminUserRole(userId, newRole, session.token)
                showRoleDialog = false
                successMessage = "¡Actualización exitosa!"
                loadData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al actualizar rol"
            } finally {
                isLoading = false
            }
        }
    }

    fun assignArea(userId: Long, areaId: Long) {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                repository.updateAdminUserArea(userId, areaId, session.token)
                showAreaDialog = false
                successMessage = "¡Área asignada exitosamente!"
                loadData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al asignar área"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleUserStatus(userId: Long, active: Boolean) {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            try {
                repository.updateAdminUserStatus(userId, active, session.token)
                loadData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cambiar estado"
            }
        }
    }

    fun clearMessages() {
        successMessage = ""
        errorMessage = ""
    }
}
