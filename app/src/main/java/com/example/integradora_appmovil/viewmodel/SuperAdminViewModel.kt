package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.AdminAreaRemote
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class SuperAdminViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var currentSession by mutableStateOf<AuthSession?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var updatingUserId by mutableStateOf<Long?>(null)
        private set

    var successMessage by mutableStateOf("")
        private set

    var savingArea by mutableStateOf(false)
        private set

    var savingUser by mutableStateOf(false)
        private set

    val users = mutableStateListOf<AdminUserRemote>()
    val areas = mutableStateListOf<AdminAreaRemote>()

    fun bindSession(session: AuthSession?) {
        currentSession = session
        if (session == null) {
            users.clear()
            areas.clear()
            errorMessage = ""
            isLoading = false
            updatingUserId = null
            successMessage = ""
            savingArea = false
            savingUser = false
            return
        }

        refreshAll()
    }

    fun refreshAll() {
        val session = currentSession ?: return
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
                users.clear()
                areas.clear()
            } catch (_: Exception) {
                errorMessage = "No se pudo cargar la información"
                users.clear()
                areas.clear()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateUserStatus(user: AdminUserRemote, activo: Boolean) {
        val session = currentSession ?: return
        if (user.correo.equals("admin", ignoreCase = true)) {
            return
        }

        viewModelScope.launch {
            updatingUserId = user.id
            errorMessage = ""
            try {
                val updated = repository.updateAdminUserStatus(user.id, activo, session.token)
                val index = users.indexOfFirst { it.id == updated.id }
                if (index >= 0) {
                    users[index] = updated
                }
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo actualizar el estado"
            } finally {
                updatingUserId = null
            }
        }
    }

    fun createArea(nombre: String, onSuccess: () -> Unit = {}) {
        val session = currentSession ?: return
        viewModelScope.launch {
            savingArea = true
            errorMessage = ""
            try {
                repository.createAdminArea(nombre, session.token)
                successMessage = "¡Área registrada exitosamente!"
                refreshAll()
                onSuccess()
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo crear el área"
            } finally {
                savingArea = false
            }
        }
    }

    fun assignDirector(areaId: Long, userId: Long, onSuccess: () -> Unit = {}) {
        val session = currentSession ?: return
        viewModelScope.launch {
            savingArea = true
            errorMessage = ""
            try {
                repository.updateAdminAreaDirector(areaId, userId, session.token)
                successMessage = "¡Área asignada exitosamente!"
                refreshAll()
                onSuccess()
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo asignar el director"
            } finally {
                savingArea = false
            }
        }
    }

    fun createUser(
        nombre: String,
        correo: String,
        contraseña: String,
        rol: String,
        onSuccess: () -> Unit = {}
    ) {
        val session = currentSession ?: return
        viewModelScope.launch {
            savingUser = true
            errorMessage = ""
            try {
                repository.createAdminUser(
                    org.json.JSONObject()
                        .put("nombre", nombre)
                        .put("correo", correo)
                        .put("contraseña", contraseña)
                        .put("rol", rol),
                    session.token
                )
                successMessage = "¡Usuario creado exitosamente!"
                refreshAll()
                onSuccess()
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo crear el usuario"
            } finally {
                savingUser = false
            }
        }
    }

    fun updateUserRole(userId: Long, rol: String, onSuccess: () -> Unit = {}) {
        val session = currentSession ?: return
        viewModelScope.launch {
            updatingUserId = userId
            errorMessage = ""
            try {
                repository.updateAdminUserRole(userId, rol, session.token)
                successMessage = "¡Actualización exitosa!"
                refreshAll()
                onSuccess()
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo actualizar el rol"
            } finally {
                updatingUserId = null
            }
        }
    }

    fun updateUserArea(userId: Long, areaId: Long, onSuccess: () -> Unit = {}) {
        val session = currentSession ?: return
        viewModelScope.launch {
            updatingUserId = userId
            errorMessage = ""
            try {
                repository.updateAdminUserArea(userId, areaId, session.token)
                successMessage = "¡Área asignada exitosamente!"
                refreshAll()
                onSuccess()
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "No se pudo asignar el área"
            } finally {
                updatingUserId = null
            }
        }
    }

    fun clearMessages() {
        errorMessage = ""
        successMessage = ""
    }

    fun logout() {
        currentSession = null
        SessionManager.clearSession()
        users.clear()
        areas.clear()
        isLoading = false
        errorMessage = ""
        updatingUserId = null
        successMessage = ""
        savingArea = false
        savingUser = false
    }
}
