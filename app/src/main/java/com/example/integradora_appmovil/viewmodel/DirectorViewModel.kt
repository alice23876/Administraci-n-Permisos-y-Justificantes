package com.example.integradora_appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.network.BinaryResponse
import com.example.integradora_appmovil.repository.DirectorRequestDetailRemote
import com.example.integradora_appmovil.repository.DirectorRequestRemote
import com.example.integradora_appmovil.repository.UserRepository
import com.example.integradora_appmovil.ui.screens.RequestItem
import kotlinx.coroutines.launch

class DirectorViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var pendingRequests = mutableStateListOf<RequestItem>()
        private set

    var historyRequests = mutableStateListOf<RequestItem>()
        private set

    var selectedRequest by mutableStateOf<RequestItem?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isDetailLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var detailErrorMessage by mutableStateOf("")
        private set

    private var currentSession: AuthSession? = null

    fun bindSession(session: AuthSession?) {
        currentSession = session
        if (session == null) {
            pendingRequests.clear()
            historyRequests.clear()
            selectedRequest = null
            errorMessage = ""
            detailErrorMessage = ""
            isLoading = false
            isDetailLoading = false
            return
        }

        refreshRequests()
    }

    fun refreshRequests() {
        val session = currentSession ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val requests = repository.getDirectorRequests(session.correo, session.token)
                applyRequestLists(requests)
            } catch (e: ApiException) {
                errorMessage = e.message
                pendingRequests.clear()
                historyRequests.clear()
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar las solicitudes"
                pendingRequests.clear()
                historyRequests.clear()
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRequest(request: RequestItem) {
        val session = currentSession ?: return
        viewModelScope.launch {
            isDetailLoading = true
            detailErrorMessage = ""
            try {
                val detail = repository.getDirectorRequestDetailByGet(session.correo, request.id.toLong(), session.token)
                selectedRequest = mapDetail(detail)
            } catch (e: ApiException) {
                detailErrorMessage = e.message
            } catch (e: Exception) {
                detailErrorMessage = "No se pudo cargar el detalle"
            } finally {
                isDetailLoading = false
            }
        }
    }

    fun clearSelectedRequest() {
        selectedRequest = null
        detailErrorMessage = ""
        isDetailLoading = false
    }

    fun updateRequestStatus(id: String, status: String, onSuccess: () -> Unit) {
        val session = currentSession ?: return
        viewModelScope.launch {
            isDetailLoading = true
            detailErrorMessage = ""
            try {
                repository.updateDirectorRequestStatus(session.correo, id.toLong(), status, session.token)
                clearSelectedRequest()
                refreshRequests()
                onSuccess()
            } catch (e: ApiException) {
                detailErrorMessage = e.message
            } catch (e: Exception) {
                detailErrorMessage = "No se pudo actualizar la solicitud"
            } finally {
                isDetailLoading = false
            }
        }
    }

    fun downloadSelectedAttachment(
        onSuccess: (BinaryResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val session = currentSession ?: return
        val request = selectedRequest ?: return
        if (!request.hasPdf) {
            onError("No existe comprobante adjunto")
            return
        }

        viewModelScope.launch {
            try {
                val file = repository.downloadDirectorRequestPdf(session.correo, request.id.toLong(), session.token)
                onSuccess(file)
            } catch (e: ApiException) {
                onError(e.message)
            } catch (_: Exception) {
                onError("No se pudo descargar el comprobante")
            }
        }
    }

    fun logout() {
        SessionManager.clearSession()
        bindSession(null)
    }

    private fun applyRequestLists(requests: List<DirectorRequestRemote>) {
        pendingRequests.clear()
        historyRequests.clear()

        requests.forEach { remote ->
            val item = RequestItem(
                id = remote.id.toString(),
                teacherName = remote.docente,
                type = normalizeType(remote.tipo),
                area = remote.area.ifBlank { "Sin asignar" },
                date = remote.fecha,
                status = normalizeStatus(remote.estado)
            )

            if (remote.estado.equals("Pendiente", ignoreCase = true)) {
                pendingRequests.add(item)
            } else {
                historyRequests.add(item)
            }
        }
    }

    private fun mapDetail(detail: DirectorRequestDetailRemote): RequestItem {
        return RequestItem(
            id = detail.id.toString(),
            teacherName = detail.docente,
            type = normalizeType(detail.tipo),
            area = detail.area.ifBlank { "Sin asignar" },
            date = detail.fecha,
            status = normalizeStatus(detail.estado),
            reason = detail.motivo,
            approvedBy = detail.aprobadoPor.ifBlank { "Sin asignar" },
            requestedDate = detail.fechaSolicitada.ifBlank { detail.fecha },
            requestedTime = detail.horaSolicitada,
            departureRegisteredAt = detail.fechaSalidaRegistrada,
            entryRegisteredAt = detail.fechaEntradaRegistrada,
            returnsSameDay = detail.regresaMismoDia,
            hasPdf = detail.tieneComprobante,
            attachmentName = detail.comprobanteNombre
        )
    }

    private fun normalizeType(type: String): String =
        type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private fun normalizeStatus(status: String): String =
        status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
