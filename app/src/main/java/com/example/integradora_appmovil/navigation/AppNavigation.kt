package com.example.integradora_appmovil.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.integradora_appmovil.model.SessionManager
import com.example.integradora_appmovil.ui.screens.*
import com.example.integradora_appmovil.viewmodel.LoginViewModel
import com.example.integradora_appmovil.viewmodel.RecoverPasswordViewModel
import com.example.integradora_appmovil.viewmodel.RegisterViewModel
import com.example.integradora_appmovil.viewmodel.SecurityGuardViewModel
import com.example.integradora_appmovil.viewmodel.TeacherViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    val teacherViewModel: TeacherViewModel = viewModel()
    val securityGuardViewModel: SecurityGuardViewModel = viewModel()
    val currentSession = SessionManager.currentUser

    LaunchedEffect(currentSession?.correo, currentSession?.token) {
        teacherViewModel.bindSession(currentSession)
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
                    val destination = if (currentSession?.rol.equals("Guardia", ignoreCase = true)) {
                        Routes.SECURITY_GUARD_HOME
                    } else {
                        Routes.TEACHER_HOME
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
                onCodeSent = { recoverPasswordViewModel.nextStep() },
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
            TeacherScreen(
                userData = teacherUserData ?: UserProfile(
                    name = "Docente",
                    status = "Activa",
                    email = currentSession?.correo.orEmpty(),
                    position = currentSession?.rol.orEmpty()
                ),
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
                onBack = { navController.popBackStack() }
            )
        }

        // HISTORIAL DE SOLICITUDES
        composable(Routes.TEACHER_HISTORY) {
            val history by teacherViewModel.historyData.collectAsState()
            val isHistoryLoading by teacherViewModel.isHistoryLoading.collectAsState()
            val historyError by teacherViewModel.historyError.collectAsState()
            HistoryScreen(
                requests = history,
                onBack = { navController.popBackStack() },
                onNewRequest = {
                    navController.navigate(Routes.TEACHER_NEW_REQUEST)
                },
                isLoading = isHistoryLoading,
                errorMessage = historyError,
                onRetry = { teacherViewModel.refreshHistory() }
            )
        }

        // NUEVO JUSTIFICANTE
        composable(Routes.TEACHER_NEW_REQUEST) {
            NewRequestScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.TEACHER_HISTORY) {
                        popUpTo(Routes.TEACHER_NEW_REQUEST) { inclusive = true }
                    }
                }
            )
        }
        // NUEVO PASE DE SALIDA
        composable(Routes.TEACHER_NEW_EXIT_PERMIT){
            NewExitPermitScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.TEACHER_HOME) {
                        popUpTo(Routes.TEACHER_NEW_EXIT_PERMIT) { inclusive = true }
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
    }
}
