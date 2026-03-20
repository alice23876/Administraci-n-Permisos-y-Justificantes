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
        //LOGIN
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.TEACHER_HOME) {
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
            TeacherScreen(
                userData = UserProfile(name = "Elena", status = "Activa"),
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TEACHER_HOME) { inclusive = true }
                    }
                },
                onProfileClick = { /* Navegar a perfil si existe */ },
                onNavigateToRequests = {
                    navController.navigate(Routes.TEACHER_HISTORY)
                }
            )
        }

        // --- HISTORIAL DE SOLICITUDES ---
        composable(Routes.TEACHER_HISTORY) {
            HistoryScreen(
                requests = listOf(), // Aquí vendrán los datos de tu repositorio/ViewModel
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
    }
}
