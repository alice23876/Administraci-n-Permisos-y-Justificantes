package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    companion object {
        private const val ALLOWED_SPECIAL_CHARACTERS = "@#$%&*.,:;!?¡¿+-=/<>[](){}_~|^\\"
    }

    // Estados
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isRegistered by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    // Validación de Nombre (no debe estar vacío)
    val isNameValid: Boolean
        get() = fullName.isNotBlank()

    // Validación de Correo Institucional
    // Solo debe de acpetar con terminacion @utez.edu.mx
    val isEmailValid: Boolean
        get() = email.trim().lowercase().matches(
            Regex("^[a-zA-Z0-9]+@(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.edu\\.mx$")
        )

    val showEmailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    // Validación de Contraseña (debe de contener mayusculas, numeros y simbolos con min 8 mensajes)
    val isPasswordComplex: Boolean
        get() {
            val hasUppercase = password.any { it.isUpperCase() }
            val hasNumber = password.any { it.isDigit() }
            val hasSymbol = password.any { ALLOWED_SPECIAL_CHARACTERS.contains(it) }
            return password.length >= 8 && hasUppercase && hasNumber && hasSymbol
        }

    val showPasswordError: Boolean
        get() = password.isNotEmpty() && !isPasswordComplex

    // Validación de Confirmación
    val passwordsMatch: Boolean
        get() = password == confirmPassword && confirmPassword.isNotEmpty()

    val showMatchError: Boolean
        get() = confirmPassword.isNotEmpty() && !passwordsMatch

    // Estado global del botón
    val isFormComplete: Boolean
        get() = isNameValid && isEmailValid && isPasswordComplex && passwordsMatch
    fun onRegisterClicked() {
        if (isFormComplete) {
            viewModelScope.launch {
                isLoading = true
                errorMessage = ""
                try {
                    repository.register(
                        fullName = fullName,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                    isRegistered = true
                } catch (e: ApiException) {
                    errorMessage = e.message
                } catch (e: Exception) {
                    errorMessage = "Error de conexión con el servidor"
                }
                isLoading = false
            }
        }
    }

    fun resetRegistration() {
        isRegistered = false
        fullName = ""
        email = ""
        password = ""
        confirmPassword = ""
        errorMessage = ""
        isLoading = false
    }
}
