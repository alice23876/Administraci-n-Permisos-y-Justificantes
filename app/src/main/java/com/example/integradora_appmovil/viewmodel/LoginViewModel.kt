package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch
class LoginViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isEmailInvalid by mutableStateOf(false)
        private set

    var isPasswordInvalid by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""
        isEmailInvalid = false
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""
        isPasswordInvalid = false
    }
    val canSubmit: Boolean
        get() = email.isNotEmpty() &&
                password.isNotEmpty() &&
                !isPasswordInvalid

    fun login(onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage = "Campos obligatorios"
            return
        }

        if (!canSubmit) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val session = repository.login(email, password)
                val isSupportedRole = session.rol.equals("Docente", ignoreCase = true) ||
                        session.rol.equals("Guardia", ignoreCase = true) ||
                        session.rol.equals("Director de area", ignoreCase = true) ||
                        session.rol.equals("Super administrador", ignoreCase = true)

                if (!isSupportedRole) {
                    SessionManager.clearSession()
                    errorMessage = "Por ahora la app móvil solo soporta docentes, directores, guardias y super admin"
                } else {
                    SessionManager.startSession(session)
                    onSuccess()
                }
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetForm() {
        email = ""
        password = ""
        errorMessage = ""
        isEmailInvalid = false
        isPasswordInvalid = false
        isLoading = false
    }
}
