package com.example.integradora_appmovil.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.TeacherRequestRemote
import com.example.integradora_appmovil.repository.UserRepository
import com.example.integradora_appmovil.ui.screens.UserProfile
import com.example.integradora_appmovil.ui.screens.RequestHistory
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData: StateFlow<UserProfile?> = _userData.asStateFlow()

    private val _historyData = MutableStateFlow<List<RequestHistory>>(emptyList())
    val historyData: StateFlow<List<RequestHistory>> = _historyData.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _historyError = MutableStateFlow("")
    val historyError: StateFlow<String> = _historyError.asStateFlow()

    private var currentSession: AuthSession? = null

    fun bindSession(session: AuthSession?) {
        currentSession = session
        if (session == null) {
            _userData.value = null
            _historyData.value = emptyList()
            _historyError.value = ""
            _isHistoryLoading.value = false
            return
        }

        _userData.value = buildUserProfile(session)
        refreshHistory()
    }

    fun refreshHistory() {
        val session = currentSession ?: return
        viewModelScope.launch {
            _isHistoryLoading.value = true
            _historyError.value = ""
            try {
                _historyData.value = repository
                    .getTeacherRequests(session.correo, session.token)
                    .map(::mapHistoryItem)
            } catch (e: ApiException) {
                _historyError.value = e.message
                _historyData.value = emptyList()
            } catch (e: Exception) {
                _historyError.value = "No se pudo cargar el historial"
                _historyData.value = emptyList()
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    fun logout() {
        currentSession = null
        SessionManager.clearSession()
        _userData.value = null
        _historyData.value = emptyList()
        _historyError.value = ""
        _isHistoryLoading.value = false
    }

    private fun buildUserProfile(session: AuthSession): UserProfile {
        val nameParts = session.nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        return UserProfile(
            name = firstName.ifEmpty { session.nombre },
            lastName = lastName,
            email = session.correo,
            area = "",
            position = session.rol,
            status = "Activa"
        )
    }

    private fun mapHistoryItem(remote: TeacherRequestRemote): RequestHistory {
        val normalizedStatus = remote.estado.trim()
        return RequestHistory(
            id = "#${remote.id}",
            type = remote.tipo.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            date = remote.fecha,
            status = normalizedStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            statusColor = when {
                normalizedStatus.equals("Aprobado", ignoreCase = true) -> SuccessGreen
                normalizedStatus.equals("Rechazado", ignoreCase = true) -> Color(0xFFE53935)
                else -> Color(0xFFF1C40F)
            }
        )
    }
}
