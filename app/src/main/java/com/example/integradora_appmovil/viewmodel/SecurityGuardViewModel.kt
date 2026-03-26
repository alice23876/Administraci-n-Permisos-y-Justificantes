package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SecurityGuardViewModel : ViewModel() {

    // Estado del campo de texto (Folio)
    var folioText by mutableStateOf("")

    // Control del diálogo de éxito
    var showSuccessDialog by mutableStateOf(false)
        private set

    // El formulario es válido si el folio no está vacío
    val isFormValid: Boolean
        get() = folioText.trim().isNotEmpty()

    // Actualiza el valor del folio desde el campo de texto manual
    fun onFolioChange(newFolio: String) {
        folioText = newFolio
    }

    // Simula la captura de un código QR
    fun onQrScanned(qrData: String) {
        folioText = qrData
    }

    // Ejecuta la validación del folio
    fun validateFolio() {
        if (isFormValid) {
            // Aquí iría la lógica de red/base de datos para validar el folio
            showSuccessDialog = true
        }
    }

    // Cierra el diálogo y reinicia los campos para un nuevo escaneo
    fun dismissSuccessDialog() {
        showSuccessDialog = false
        folioText = ""
    }
}