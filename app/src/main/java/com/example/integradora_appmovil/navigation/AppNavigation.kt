package com.example.integradora_appmovil.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.integradora_appmovil.ui.screens.LoginScreen
import com.example.integradora_appmovil.ui.screens.RecoverPasswordScreen
import com.example.integradora_appmovil.ui.screens.RegisterScreen
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

        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    Toast.makeText(
                        navController.context,
                        "¡Bienvenido!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Aquí podrías navegar a la pantalla principal (Home) si la tuvieras
                    // navController.navigate(Routes.HOME)
                },
                onRegisterClick = {
                    // CORRECCIÓN: Navegar a la pantalla de registro
                    navController.navigate(Routes.REGISTER)
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.RECOVER)
                }
            )
        }
        composable(Routes.RECOVER) {
            val recoverPasswordViewModel: RecoverPasswordViewModel = viewModel()
            RecoverPasswordScreen(
                viewModel = recoverPasswordViewModel,
                onBackToLogin = {
                    navController.popBackStack()
                },
                onCodeSent = {
                    recoverPasswordViewModel.nextStep()
                },
                onFinish = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onBackToLogin = {
                    navController.popBackStack() // Mejor usar popBackStack para volver
                },
                onRegisterSuccess = {
                    Toast.makeText(
                        navController.context,
                        "Cuenta creada con éxito",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
