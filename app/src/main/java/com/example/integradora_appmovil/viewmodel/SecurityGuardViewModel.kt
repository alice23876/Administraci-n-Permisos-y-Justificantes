package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.GuardFolioValidationRemote
import com.example.integradora_appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class SecurityGuardViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var folioText by mutableStateOf("")
        private set

    var showSuccessDialog by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var validationResult by mutableStateOf<GuardFolioValidationRemote?>(null)
        private set

    val isFormValid: Boolean
        get() = folioText.trim().isNotEmpty()

    fun onFolioChange(newFolio: String) {
        folioText = newFolio
        if (errorMessage.isNotEmpty()) {
            errorMessage = ""
        }
    }

    fun onQrScanned(qrData: String) {
        folioText = qrData
        if (errorMessage.isNotEmpty()) {
            errorMessage = ""
        }
    }

    fun validateFolio() {
        if (!isFormValid) {
            errorMessage = "Ingresa un folio válido"
            return
        }

        val session = SessionManager.currentUser
        if (session == null || !session.rol.equals("Guardia", ignoreCase = true)) {
            errorMessage = "La sesión de guardia no está disponible"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                validationResult = repository.validateGuardFolio(folioText, session.token)
                showSuccessDialog = true
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (e: Exception) {
                errorMessage = "No se pudo validar el folio"
            } finally {
                isLoading = false
            }
        }
    }

    fun dismissSuccessDialog() {
        showSuccessDialog = false
        folioText = ""
        validationResult = null
    }
}
