package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecoverPasswordViewModel : ViewModel() {
    // Control de flujo: 1 = Correo, 2 = Código, 3 = Nueva Contraseña, 4 = Éxito Final
    var currentStep by mutableStateOf(1)
        private set
    // Estados para el Paso 1 (Correo)
    var email by mutableStateOf("")
        private set

    val isEmailValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val showEmailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    // Estados para el Paso 2 (Código)
    var code by mutableStateOf("")
        private set

    var isCodeInvalid by mutableStateOf(false)
        private set

    var showResendSuccess by mutableStateOf(false)
        private set

    val isCodeComplete: Boolean
        get() = code.length == 5

    // Estados para el Paso 3 (Nueva Contraseña)
    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    val passwordsMatch: Boolean
        get() = newPassword == confirmPassword && newPassword.isNotEmpty()

    val isFormValid: Boolean
        get() = newPassword.length >= 8 && confirmPassword.length >= 8 && passwordsMatch

    val showMatchError: Boolean
        get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword

    // Funciones de actualización de estado
    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onCodeChange(newValue: String) {
        if (newValue.length <= 5) {
            code = newValue
            isCodeInvalid = false
        }
    }

    fun onNewPasswordChange(newValue: String) {
        newPassword = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    // Acciones de navegación y lógica
    fun nextStep() {
        if (currentStep < 4) currentStep++
    }

    fun previousStep(onBackToLogin: () -> Unit) {
        if (currentStep > 1) {
            currentStep--
            isCodeInvalid = false
            showResendSuccess = false
        } else {
            onBackToLogin()
        }
    }

    fun verifyCode() {
        // Simulación de verificación (Hardcoded 12345 como pediste anteriormente)
        if (code == "12345") {
            nextStep()
        } else {
            isCodeInvalid = true
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            showResendSuccess = true
            delay(3000)
            showResendSuccess = false
        }
    }
}