package com.example.integradora_appmovil.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.TeacherQrRemote
import com.example.integradora_appmovil.repository.TeacherRequestDetailRemote
import com.example.integradora_appmovil.repository.TeacherRequestRemote
import com.example.integradora_appmovil.repository.UserRepository
import com.example.integradora_appmovil.ui.screens.RequestHistory
import com.example.integradora_appmovil.ui.screens.TeacherQrState
import com.example.integradora_appmovil.ui.screens.TeacherRequestDetailState
import com.example.integradora_appmovil.ui.screens.UserProfile
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData: StateFlow<UserProfile?> = _userData.asStateFlow()

    private val _historyData = MutableStateFlow<List<RequestHistory>>(emptyList())
    val historyData: StateFlow<List<RequestHistory>> = _historyData.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _historyError = MutableStateFlow("")
    val historyError: StateFlow<String> = _historyError.asStateFlow()

    private val _selectedRequestDetail = MutableStateFlow<TeacherRequestDetailState?>(null)
    val selectedRequestDetail: StateFlow<TeacherRequestDetailState?> = _selectedRequestDetail.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _detailError = MutableStateFlow("")
    val detailError: StateFlow<String> = _detailError.asStateFlow()

    private val _selectedQr = MutableStateFlow<TeacherQrState?>(null)
    val selectedQr: StateFlow<TeacherQrState?> = _selectedQr.asStateFlow()

    private val _isQrLoading = MutableStateFlow(false)
    val isQrLoading: StateFlow<Boolean> = _isQrLoading.asStateFlow()

    private val _qrError = MutableStateFlow("")
    val qrError: StateFlow<String> = _qrError.asStateFlow()

    private var currentSession: AuthSession? = null

    fun bindSession(session: AuthSession?) {
        currentSession = session
        if (session == null) {
            _userData.value = null
            _historyData.value = emptyList()
            _historyError.value = ""
            _isHistoryLoading.value = false
            clearRequestDetail()
            clearQr()
            return
        }

        _userData.value = buildUserProfile(session)
        refreshHistory()
    }

    fun refreshHistory() {
        val session = currentSession ?: return
        viewModelScope.launch {
            _isHistoryLoading.value = true
            _historyError.value = ""
            try {
                _historyData.value = repository
                    .getTeacherRequests(session.correo, session.token)
                    .map(::mapHistoryItem)
            } catch (e: ApiException) {
                _historyError.value = e.message
                _historyData.value = emptyList()
            } catch (_: Exception) {
                _historyError.value = "No se pudo cargar el historial"
                _historyData.value = emptyList()
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    fun openRequestDetail(request: RequestHistory) {
        val session = currentSession ?: return
        viewModelScope.launch {
            _isDetailLoading.value = true
            _detailError.value = ""
            _selectedRequestDetail.value = null
            try {
                val detail = repository.getTeacherRequestDetail(session.correo, request.numericId, session.token)
                _selectedRequestDetail.value = mapTeacherDetail(detail, request.directivo)
            } catch (e: ApiException) {
                _detailError.value = e.message
            } catch (_: Exception) {
                _detailError.value = "No se pudo cargar la solicitud"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun clearRequestDetail() {
        _selectedRequestDetail.value = null
        _isDetailLoading.value = false
        _detailError.value = ""
    }

    fun openQr(request: RequestHistory) {
        val session = currentSession ?: return
        viewModelScope.launch {
            _isQrLoading.value = true
            _qrError.value = ""
            _selectedQr.value = null
            try {
                val qr = repository.getTeacherRequestQr(session.correo, request.numericId, session.token)
                _selectedQr.value = mapTeacherQr(qr)
            } catch (e: ApiException) {
                _qrError.value = e.message
            } catch (_: Exception) {
                _qrError.value = "No se pudo cargar el QR"
            } finally {
                _isQrLoading.value = false
            }
        }
    }

    fun clearQr() {
        _selectedQr.value = null
        _isQrLoading.value = false
        _qrError.value = ""
    }

    fun logout() {
        currentSession = null
        SessionManager.clearSession()
        _userData.value = null
        _historyData.value = emptyList()
        _historyError.value = ""
        _isHistoryLoading.value = false
        clearRequestDetail()
        clearQr()
    }

    private fun buildUserProfile(session: AuthSession): UserProfile {
        val nameParts = session.nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        return UserProfile(
            name = firstName.ifEmpty { session.nombre },
            lastName = lastName,
            email = session.correo,
            area = "",
            position = session.rol,
            status = "Activa"
        )
    }

    private fun mapHistoryItem(remote: TeacherRequestRemote): RequestHistory {
        val normalizedStatus = remote.estado.trim()
        return RequestHistory(
            numericId = remote.id,
            id = "#${remote.id}",
            type = remote.tipo.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            directivo = remote.directivo,
            area = remote.area,
            date = remote.fecha,
            status = normalizedStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            statusColor = when {
                normalizedStatus.equals("Aprobado", ignoreCase = true) -> SuccessGreen
                normalizedStatus.equals("Rechazado", ignoreCase = true) -> Color(0xFFE53935)
                else -> Color(0xFFF1C40F)
            },
            canViewQr = remote.puedeVerQr
        )
    }

    private fun mapTeacherDetail(detail: TeacherRequestDetailRemote, directivo: String): TeacherRequestDetailState {
        return TeacherRequestDetailState(
            id = detail.id,
            tipo = detail.tipo,
            directivo = if (detail.aprobadoPor.isNotBlank()) detail.aprobadoPor else directivo,
            area = detail.area,
            fecha = detail.fecha,
            estado = detail.estado,
            motivo = detail.motivo,
            fechaSolicitada = detail.fechaSolicitada,
            horaSolicitada = detail.horaSolicitada,
            fechaSalidaRegistrada = detail.fechaSalidaRegistrada,
            fechaEntradaRegistrada = detail.fechaEntradaRegistrada,
            regresaMismoDia = detail.regresaMismoDia,
            tieneComprobante = detail.tieneComprobante,
            comprobanteNombre = detail.comprobanteNombre
        )
    }

    private fun mapTeacherQr(qr: TeacherQrRemote): TeacherQrState {
        return TeacherQrState(
            solicitudId = qr.solicitudId,
            qrValue = qr.qrValue,
            folio = qr.folio,
            aprobadoPor = qr.aprobadoPor,
            validoPara = qr.validoPara,
            usosRestantes = qr.usosRestantes
        )
    }
}
