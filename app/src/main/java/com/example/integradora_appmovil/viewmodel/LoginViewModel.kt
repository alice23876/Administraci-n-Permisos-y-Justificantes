package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch
class LoginViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var user by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isUserInvalid by mutableStateOf(false)
        private set

    var isPasswordInvalid by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onUserChange(newValue: String) {
        user = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""
        isUserInvalid = newValue.isNotEmpty() && newValue.length < 4
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""

        // La contraseña es inválida si tiene texto pero menos de 6 caracteres
        isPasswordInvalid = newValue.isNotEmpty() && newValue.length < 6
    }
    val canSubmit: Boolean
        get() = user.isNotEmpty() &&
                password.isNotEmpty() &&
                !isUserInvalid &&
                !isPasswordInvalid

    fun login(onSuccess: () -> Unit) {
        if (user.isEmpty() || password.isEmpty()) {
            errorMessage = "Campos obligatorios"
            return
        }

        if (!canSubmit) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val isAuthenticated = repository.authenticate(user, password)
                if (isAuthenticated != null) {
                    onSuccess()
                } else {
                    errorMessage = "Usuario o contraseña incorrectos"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }
}