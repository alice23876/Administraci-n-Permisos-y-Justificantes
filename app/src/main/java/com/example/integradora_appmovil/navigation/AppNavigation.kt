package com.example.integradora_appmovil.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.integradora_appmovil.ui.screens.*
import com.example.integradora_appmovil.viewmodel.LoginViewModel
import com.example.integradora_appmovil.viewmodel.RecoverPasswordViewModel
import com.example.integradora_appmovil.viewmodel.RegisterViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // LOGIN
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val role = loginViewModel.userRole
                    val destino = if (role == "security") {
                        Routes.SECURITY_GUARD_HOME
                    } else {
                        Routes.TEACHER_HOME
                    }

                    navController.navigate(destino) {
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
            val registerViewModel: RegisterViewModel = viewModel()
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
            val loginViewModel: LoginViewModel = viewModel()
            TeacherScreen(
                userData = UserProfile(
                    name = loginViewModel.userRole.ifEmpty { "Docente" },
                    status = "Activa",
                    lastName = "Pérez", // Datos simulados, vendrán de tu BD
                    area = "DATIC",
                    position = "Docente",
                    email = "docente@utez.edu.mx"
                ),
                onLogout = {
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
            val loginViewModel: LoginViewModel = viewModel()
            ProfileScreen(
                userData = UserProfile(
                    name = loginViewModel.userRole.ifEmpty { "Docente" },
                    lastName = "Pérez",
                    area = "DATIC",
                    position = "Docente",
                    email = "docente@utez.edu.mx",
                    status = "Activa"
                ),
                onBack = { navController.popBackStack() }
            )
        }

        // HISTORIAL DE SOLICITUDES
        composable(Routes.TEACHER_HISTORY) {
            HistoryScreen(
                requests = listOf(),
                onBack = { navController.popBackStack() },
                onNewRequest = {
                    navController.navigate(Routes.TEACHER_NEW_REQUEST)
                }
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
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SECURITY_GUARD_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
