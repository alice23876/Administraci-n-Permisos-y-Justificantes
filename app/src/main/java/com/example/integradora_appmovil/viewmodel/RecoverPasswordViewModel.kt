package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecoverPasswordViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    companion object {
        private const val ALLOWED_SPECIAL_CHARACTERS = "@#$%&*.,:;!?¡¿+-=/<>[](){}_~|^\\"
    }

    var currentStep by mutableStateOf(1)
        private set

    var email by mutableStateOf("")
        private set

    var code by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var isCodeInvalid by mutableStateOf(false)
        private set

    var showResendSuccess by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    val isEmailValid: Boolean
        get() = email.trim().matches(
            Regex("^[a-zA-Z0-9]+@(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.edu\\.mx$")
        )

    val showEmailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    val isCodeComplete: Boolean
        get() = code.length == 5

    val isPasswordComplex: Boolean
        get() {
            val hasUppercase = newPassword.any { it.isUpperCase() }
            val hasNumber = newPassword.any { it.isDigit() }
            val hasSymbol = newPassword.any { ALLOWED_SPECIAL_CHARACTERS.contains(it) }
            return newPassword.length >= 8 && hasUppercase && hasNumber && hasSymbol
        }

    val passwordsMatch: Boolean
        get() = newPassword == confirmPassword && newPassword.isNotEmpty()

    val isFormValid: Boolean
        get() = isPasswordComplex && confirmPassword.length >= 8 && passwordsMatch

    val showMatchError: Boolean
        get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword

    val showPasswordComplexityError: Boolean
        get() = newPassword.isNotEmpty() && !isPasswordComplex

    fun onEmailChange(newValue: String) {
        email = newValue
        clearError()
    }

    fun onCodeChange(newValue: String) {
        if (newValue.length <= 5) {
            code = newValue
            isCodeInvalid = false
            clearError()
        }
    }

    fun onNewPasswordChange(newValue: String) {
        newPassword = newValue
        clearError()
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        clearError()
    }

    fun previousStep(onBackToLogin: () -> Unit) {
        if (currentStep > 1) {
            currentStep--
            isCodeInvalid = false
            showResendSuccess = false
            errorMessage = ""
        } else {
            onBackToLogin()
        }
    }

    fun requestRecoveryCode(onSuccess: (() -> Unit)? = null) {
        if (!isEmailValid) {
            errorMessage = "Correo inválido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                repository.requestRecoveryCode(email)
                currentStep = 2
                onSuccess?.invoke()
            } catch (e: ApiException) {
                errorMessage = e.message ?: "No se pudo enviar el código"
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyCode() {
        if (!isCodeComplete) {
            isCodeInvalid = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                repository.verifyRecoveryCode(email, code)
                currentStep = 3
                isCodeInvalid = false
            } catch (e: ApiException) {
                isCodeInvalid = true
                errorMessage = e.message ?: "Código inválido"
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    fun resendCode() {
        if (!isEmailValid) {
            errorMessage = "Correo inválido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                repository.requestRecoveryCode(email)
                showResendSuccess = true
                delay(3000)
                showResendSuccess = false
            } catch (e: ApiException) {
                errorMessage = e.message ?: "No se pudo reenviar el código"
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword() {
        if (!isFormValid) {
            errorMessage = "Revisa la nueva contraseña"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                repository.resetRecoveryPassword(email, code, newPassword, confirmPassword)
                currentStep = 4
            } catch (e: ApiException) {
                errorMessage = e.message ?: "No se pudo actualizar la contraseña"
            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        if (errorMessage.isNotEmpty()) {
            errorMessage = ""
        }
    }
}
