package com.example.integradora_appmovil.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.ui.screens.*
import com.example.integradora_appmovil.util.PdfFileHandler
import com.example.integradora_appmovil.viewmodel.LoginViewModel
import com.example.integradora_appmovil.viewmodel.RecoverPasswordViewModel
import com.example.integradora_appmovil.viewmodel.RegisterViewModel
import com.example.integradora_appmovil.viewmodel.DirectorViewModel
import com.example.integradora_appmovil.viewmodel.SecurityGuardViewModel
import com.example.integradora_appmovil.viewmodel.SuperAdminViewModel
import com.example.integradora_appmovil.viewmodel.TeacherViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    val teacherViewModel: TeacherViewModel = viewModel()
    val directorViewModel: DirectorViewModel = viewModel()
    val securityGuardViewModel: SecurityGuardViewModel = viewModel()
    val superAdminViewModel: SuperAdminViewModel = viewModel()
    val currentSession = SessionManager.currentUser

    LaunchedEffect(currentSession?.correo, currentSession?.token) {
        teacherViewModel.bindSession(currentSession)
        directorViewModel.bindSession(currentSession)
        superAdminViewModel.bindSession(currentSession)
    }
    
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val activeSession = SessionManager.currentUser
                    val destination = when {
                        activeSession?.rol.equals("Super administrador", ignoreCase = true) -> Routes.SUPER_ADMIN_HOME
                        activeSession?.rol.equals("Guardia", ignoreCase = true) -> Routes.SECURITY_GUARD_HOME
                        activeSession?.rol.equals("Director de area", ignoreCase = true) -> Routes.DIRECTOR_HOME
                        else -> Routes.TEACHER_HOME
                    }

                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.RECOVER)
                }
            )
        }

        // REGISTRO
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = registerViewModel,
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    Toast.makeText(navController.context, "Cuenta creada", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // RECUPERAR CONTRASEÑA
        composable(Routes.RECOVER) {
            val recoverPasswordViewModel: RecoverPasswordViewModel = viewModel()
            RecoverPasswordScreen(
                viewModel = recoverPasswordViewModel,
                onBackToLogin = { navController.popBackStack() },
                onCodeSent = { },
                onFinish = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.RECOVER) { inclusive = true }
                    }
                }
            )
        }

        // INICIO DOCENTE
        composable(Routes.TEACHER_HOME) {
            val teacherUserData by teacherViewModel.userData.collectAsState()
            val teacherHistory by teacherViewModel.historyData.collectAsState()
            TeacherScreen(
                userData = teacherUserData ?: UserProfile(
                    name = "Docente",
                    status = "Activa",
                    email = currentSession?.correo.orEmpty(),
                    position = currentSession?.rol.orEmpty()
                ),
                hasTodayExitPermit = teacherHistory.any {
                    it.type.contains("permiso", ignoreCase = true) &&
                        it.requestDate == java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                },
                hasTodayJustificante = teacherHistory.any {
                    it.type.contains("justificante", ignoreCase = true) &&
                        it.requestDate == java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                },
                onLogout = {
                    teacherViewModel.logout()
                    loginViewModel.resetForm()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TEACHER_HOME) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.TEACHER_PROFILE)
                },
                onNavigateToRequests = {
                    navController.navigate(Routes.TEACHER_HISTORY)
                },
                onNavigateToNewRequest = {
                    navController.navigate(Routes.TEACHER_NEW_REQUEST)
                },
                onNavigateToNewExitPermit= {
                    navController.navigate(Routes.TEACHER_NEW_EXIT_PERMIT){
                        launchSingleTop = true
                    }
                }
            )
        }

        // PERFIL DOCENTE
        composable(Routes.TEACHER_PROFILE) {
            val teacherUserData by teacherViewModel.userData.collectAsState()
            ProfileScreen(
                userData = teacherUserData,
                session = currentSession,
                onBack = { navController.popBackStack() },
                onLogout = {
                    teacherViewModel.logout()
                    loginViewModel.resetForm()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TEACHER_HOME) { inclusive = true }
                    }
                }
            )
        }

        // HISTORIAL DE SOLICITUDES
        composable(Routes.TEACHER_HISTORY) {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val history by teacherViewModel.historyData.collectAsState()
            val isHistoryLoading by teacherViewModel.isHistoryLoading.collectAsState()
            val historyError by teacherViewModel.historyError.collectAsState()
            val selectedDetail by teacherViewModel.selectedRequestDetail.collectAsState()
            val isDetailLoading by teacherViewModel.isDetailLoading.collectAsState()
            val detailError by teacherViewModel.detailError.collectAsState()
            val selectedQr by teacherViewModel.selectedQr.collectAsState()
            val isQrLoading by teacherViewModel.isQrLoading.collectAsState()
            val qrError by teacherViewModel.qrError.collectAsState()

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        teacherViewModel.refreshHistory()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            HistoryScreen(
                requests = history,
                onBack = { navController.popBackStack() },
                onNewRequest = {
                    navController.navigate(Routes.TEACHER_NEW_REQUEST)
                },
                isLoading = isHistoryLoading,
                errorMessage = historyError,
                onRetry = { teacherViewModel.refreshHistory() },
                onViewDetail = { teacherViewModel.openRequestDetail(it) },
                onViewQr = { teacherViewModel.openQr(it) },
                selectedDetail = selectedDetail,
                isDetailLoading = isDetailLoading,
                detailError = detailError,
                onDismissDetail = { teacherViewModel.clearRequestDetail() },
                selectedQr = selectedQr,
                isQrLoading = isQrLoading,
                qrError = qrError,
                onDismissQr = { teacherViewModel.clearQr() },
                onDownloadAttachment = {
                    teacherViewModel.downloadSelectedAttachment(
                        onSuccess = { file ->
                            PdfFileHandler.openPdf(context, file.fileName, file.bytes, file.contentType)
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }

        // NUEVO JUSTIFICANTE
        composable(Routes.TEACHER_NEW_REQUEST) {
            val context = LocalContext.current
            val teacherHistory by teacherViewModel.historyData.collectAsState()
            val isJustificanteSubmitting by teacherViewModel.isJustificanteSubmitting.collectAsState()
            val justificanteError by teacherViewModel.justificanteError.collectAsState()
            val blockedJustificanteDates = teacherHistory
                .filter { it.type.contains("justificante", ignoreCase = true) }
                .mapNotNull { historyItem ->
                    val parts = historyItem.date.split("/")
                    if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
                }
                .toSet()
            NewRequestScreen(
                blockedJustificanteDates = blockedJustificanteDates,
                isSubmitting = isJustificanteSubmitting,
                submitError = justificanteError,
                onSubmit = { fechaIncidencia, motivo, comprobanteUri, onSuccess ->
                    teacherViewModel.createJustificante(
                        fechaIncidencia = fechaIncidencia,
                        motivo = motivo,
                        comprobanteUri = comprobanteUri,
                        contentResolver = context.contentResolver,
                        onSuccess = onSuccess
                    )
                },
                onBack = { navController.popBackStack() },
                onSuccess = {
                    teacherViewModel.clearJustificanteFeedback()
                    navController.navigate(Routes.TEACHER_HISTORY) {
                        popUpTo(Routes.TEACHER_NEW_REQUEST) { inclusive = true }
                    }
                }
            )
        }
        // NUEVO PASE DE SALIDA
        composable(Routes.TEACHER_NEW_EXIT_PERMIT){
            val teacherHistory by teacherViewModel.historyData.collectAsState()
            val isExitPermitSubmitting by teacherViewModel.isExitPermitSubmitting.collectAsState()
            val exitPermitError by teacherViewModel.exitPermitError.collectAsState()

            NewExitPermitScreen(
                hasTodayExitPermit = teacherHistory.any {
                    it.type.contains("permiso", ignoreCase = true) &&
                        it.requestDate == java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                },
                isSubmitting = isExitPermitSubmitting,
                submitError = exitPermitError,
                onSubmit = { horaSalida, regresaMismoDia, motivo, onSuccess ->
                    teacherViewModel.createExitPermit(
                        horaSalida = horaSalida,
                        regresaMismoDia = regresaMismoDia,
                        motivo = motivo,
                        onSuccess = onSuccess
                    )
                },
                onBack = { navController.popBackStack() },
                onSuccess = {
                    teacherViewModel.clearExitPermitFeedback()
                    navController.navigate(Routes.TEACHER_HOME) {
                        popUpTo(Routes.TEACHER_NEW_EXIT_PERMIT) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DIRECTOR_HOME) {
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        directorViewModel.refreshRequests()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            DirectorScreen(
                viewModel = directorViewModel,
                session = currentSession,
                onLogout = {
                    directorViewModel.logout()
                    loginViewModel.resetForm()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DIRECTOR_HOME) { inclusive = true }
                    }
                }
            )
        }

        //PERFIL GUARDIA DE SEGURIDAD
        composable(Routes.SECURITY_GUARD_HOME) {
            SecurityGuardScreen(
                viewModel = securityGuardViewModel,
                onLogout = {
                    securityGuardViewModel.dismissSuccessDialog()
                    loginViewModel.resetForm()
                    SessionManager.clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SECURITY_GUARD_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SUPER_ADMIN_HOME) {
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        superAdminViewModel.refreshAll()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SuperAdminScreen(
                viewModel = superAdminViewModel,
                session = currentSession,
                onLogout = {
                    superAdminViewModel.logout()
                    loginViewModel.resetForm()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SUPER_ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
