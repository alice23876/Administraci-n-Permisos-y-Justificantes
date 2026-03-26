package com.example.integradora_appmovil.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.integradora_appmovil.ui.screens.UserProfile
import com.example.integradora_appmovil.ui.screens.RequestHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.integradora_appmovil.ui.theme.SuccessGreen


class TeacherViewModel : ViewModel() {

    // estado de navegacion
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // datos de usuario
    private val _userData = MutableStateFlow(UserProfile(name = "Elena", status = "Activa"))
    val userData: StateFlow<UserProfile> = _userData.asStateFlow()

    // historial de solicitudes
    private val _historyData = MutableStateFlow<List<RequestHistory>>(emptyList())
    val historyData: StateFlow<List<RequestHistory>> = _historyData.asStateFlow()

    // estado del formulario de nueva solicitud-
    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _motive = MutableStateFlow("")
    val motive: StateFlow<String> = _motive.asStateFlow()

    private val _selectedPdfUri = MutableStateFlow<Uri?>(null)
    val selectedPdfUri: StateFlow<Uri?> = _selectedPdfUri.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean> = _showSuccessDialog.asStateFlow()

    init {
        loadHistory()
    }
    private fun loadHistory() {
        _historyData.value = listOf(
            RequestHistory("#1254", "Justificante", "15/03/2026", "Aprobado", SuccessGreen),
            RequestHistory("#1250", "Pase de Salida", "12/03/2026", "Pendiente", Color(0xFFF1C40F))
        )
    }

    // acciones de navegacion
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // acciones del formulario
    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun onMotiveChange(newMotive: String) {
        _motive.value = newMotive
    }

    fun onPdfSelected(uri: Uri?) {
        _selectedPdfUri.value = uri
    }
    // procesa el envio de solicitued
    fun submitRequest() {
        if (isFormValid()) {
            // lógica para guardar en la base de datos
            _showSuccessDialog.value = true
        }
    }

    fun dismissSuccessDialog() {
        _showSuccessDialog.value = false
        resetForm()
        navigateTo("home")
    }
    // valida que los campos que pide esten completos
    fun isFormValid(): Boolean {
        return _selectedDate.value.isNotEmpty() && _motive.value.length >= 10
    }

    private fun resetForm() {
        _selectedDate.value = ""
        _motive.value = ""
        _selectedPdfUri.value = null
    }

    fun logout() {
        // limpia sesion, cierra sesion y vuelve al inicio
        println("Cerrando sesión...")
    }
}