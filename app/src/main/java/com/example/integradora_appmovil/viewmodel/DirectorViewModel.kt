package com.example.integradora_appmovil.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.data.local.AppDatabase
import com.example.integradora_appmovil.repository.SolicitudRepository
import com.example.integradora_appmovil.ui.screens.RequestItem
import kotlinx.coroutines.launch

class DirectorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SolicitudRepository
    
    var pendingRequests = mutableStateListOf<RequestItem>()
        private set

    var historyRequests = mutableStateListOf<RequestItem>()
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        val dao = AppDatabase.getDatabase(application).appDao()
        repository = SolicitudRepository(dao)
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            isLoading = true
            try {
                val allEntities = repository.getAllSolicitudesFromDb()
                
                pendingRequests.clear()
                historyRequests.clear()

                allEntities.forEach { entity ->
                    val teacher = repository.getUserById(entity.empleado_id)
                    val item = RequestItem(
                        id = entity.id.toString(),
                        teacherName = teacher?.nombre ?: "Desconocido",
                        type = entity.tipo,
                        date = entity.fecha_solicitud,
                        status = entity.estado,
                        reason = entity.motivo
                    )
                    
                    if (entity.estado == "PENDIENTE") {
                        pendingRequests.add(item)
                    } else {
                        historyRequests.add(item)
                    }
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading = false
            }
        }
    }

    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateSolicitudStatus(id.toLong(), status)
            loadRequests() // Recargar listas
        }
    }
}
