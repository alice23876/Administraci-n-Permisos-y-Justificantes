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

class UserStatusViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var searchTerm by mutableStateOf("")

    val users = mutableStateListOf<AdminUserRemote>()

    fun loadUsers() {
        val session = SessionManager.currentUser ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response = repository.getAdminUsers(session.token)
                users.clear()
                users.addAll(response)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar usuarios"
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
                loadUsers()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cambiar estado"
            }
        }
    }
}
