package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    // Estados de los campos
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isRegistered by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    // --- VALIDACIONES ---

    // 0. Validación de Nombre (que no esté vacío)
    val isNameValid: Boolean
        get() = fullName.isNotBlank()

    // 1. Validación de Correo Institucional
    // ACTUALIZACIÓN: Ahora acepta tanto @institucion.edu.mx como @utez.edu.mx
    val isEmailValid: Boolean
        get() = email.endsWith("@institucion.edu.mx") || email.endsWith("@utez.edu.mx")

    val showEmailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    // 2. Validación de Contraseña (Mayúsculas, Números, Símbolos)
    val isPasswordComplex: Boolean
        get() {
            val hasUppercase = password.any { it.isUpperCase() }
            val hasNumber = password.any { it.isDigit() }
            val hasSymbol = password.any { !it.isLetterOrDigit() }
            return password.length >= 8 && hasUppercase && hasNumber && hasSymbol
        }

    val showPasswordError: Boolean
        get() = password.isNotEmpty() && !isPasswordComplex

    // 3. Validación de Confirmación
    val passwordsMatch: Boolean
        get() = password == confirmPassword && confirmPassword.isNotEmpty()

    val showMatchError: Boolean
        get() = confirmPassword.isNotEmpty() && !passwordsMatch

    // Estado global del botón
    val isFormComplete: Boolean
        get() = isNameValid && isEmailValid && isPasswordComplex && passwordsMatch

    /**
     * Intenta realizar el registro del usuario.
     */
    fun onRegisterClicked() {
        if (isFormComplete) {
            viewModelScope.launch {
                isLoading = true
                // El username será la parte del correo antes del '@'
                val username = email.substringBefore("@")
                val success = repository.register(username, password, email)
                if (success) {
                    isRegistered = true
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
    }
}
