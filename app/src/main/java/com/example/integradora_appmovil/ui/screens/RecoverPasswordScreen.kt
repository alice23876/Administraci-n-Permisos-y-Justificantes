package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.integradora_appmovil.viewmodel.RecoverPasswordViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun RecoverPasswordPreview() {
    val viewModel: RecoverPasswordViewModel = viewModel()
    RecoverPasswordScreen(
        viewModel = viewModel,
        onBackToLogin = {},
        onCodeSent = {},
        onFinish = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    viewModel: RecoverPasswordViewModel = viewModel(),
    onBackToLogin: () -> Unit,
    onCodeSent: () -> Unit,
    onFinish: () -> Unit
){
    val currentStep = viewModel.currentStep
    val email = viewModel.email
    val showEmailError = viewModel.showEmailError
    val code = viewModel.code
    val isCodeInvalid = viewModel.isCodeInvalid
    val showResendSuccess = viewModel.showResendSuccess
    val isCodeComplete = viewModel.isCodeComplete
    val newPassword = viewModel.newPassword
    val confirmPassword = viewModel.confirmPassword
    val isFormValid = viewModel.isFormValid
    val showMatchError = viewModel.showMatchError
    val showPasswordComplexityError = viewModel.showPasswordComplexityError
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueBG)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Ocultar el enlace de volver si ya estamos en la pantalla de éxito
        if (currentStep < 4) {
            TextButton(
                onClick = {
                    viewModel.previousStep(onBackToLogin)
                },
                modifier = Modifier.align(Alignment.Start).padding(top = 16.dp)
            ) {
                Text(
                    text = "Regresar",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(if (currentStep < 4) 40.dp else 100.dp))

        // --- ICONO SUPERIOR ---
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(12.dp),
            color = IconBoxColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = DarkBlueBG
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TÍTULOS DINÁMICOS ---
        Text(
            text = when(currentStep) {
                1 -> "Recuperación de contraseña"
                2 -> "Código de recuperación"
                3 -> "Código de recuperación"
                else -> "¡Contraseña actualizada!"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = when(currentStep) {
                1 -> "Ingresa tu correo y te enviaremos un código de verificación."
                2 -> "Enviamos un código a ${if(email.isNotEmpty()) email else "ejemplo@inst.edu.mx"}"
                3 -> "Enviamos un código a ${if(email.isNotEmpty()) email else "ejemplo@inst.edu.mx"}"
                else -> "Tu contraseña fue cambiada correctamente.\nYa puedes iniciar sesión con tus nuevas credenciales."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- INDICADOR DE PASOS (Solo visible del 1 al 3) ---
        if (currentStep < 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCircle(number = "1", label = "Correo", isActive = currentStep >= 1)
                Box(modifier = Modifier.width(50.dp).height(1.dp).background(if(currentStep > 1) EmeraldButton else Color.Gray))
                StepCircle(number = "2", label = "Código", isActive = currentStep >= 2)
                Box(modifier = Modifier.width(50.dp).height(1.dp).background(if(currentStep > 2) EmeraldButton else Color.Gray))
                StepCircle(number = "3", label = "Contraseña", isActive = currentStep >= 3)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- CONTENIDO DINÁMICO ---
        when (currentStep) {
                1 -> {
                    EmailStepContent(
                        email = email,
                        onEmailChange = viewModel::onEmailChange,
                        isError = showEmailError,
                        onNext = { viewModel.requestRecoveryCode(onCodeSent) },
                        isValid = viewModel.isEmailValid,
                        isLoading = isLoading
                    )
                }
                2 -> {
                    CodeStepContent(
                        code = code,
                        onCodeChange = viewModel::onCodeChange,
                        onVerify = viewModel::verifyCode,
                        isValid = isCodeComplete,
                        isInvalid = isCodeInvalid,
                        onResend = viewModel::resendCode,
                        showResendSuccess = showResendSuccess,
                        isLoading = isLoading
                    )
                }
                3 -> {
                    PasswordStepContent(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        onNewPasswordChange = viewModel::onNewPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        showMatchError = showMatchError,
                        showPasswordComplexityError = showPasswordComplexityError,
                        isValid = isFormValid,
                        onFinish = viewModel::resetPassword,
                        isLoading = isLoading
                    )
                }
                4 -> {
                // PANTALLA DE ÉXITO FINAL
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldButton)
                ) {
                    Text("Ir a Inicio de Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        if (currentStep < 4) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.width(250.dp).height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
        }
    }
}

// Componentes secundarios (EmailStepContent, CodeStepContent, PasswordStepContent, StepCircle)
// Se mantienen igual que en la versión anterior para asegurar la lógica de validación solicitada.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailStepContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    onNext: () -> Unit,
    isValid: Boolean,
    isLoading: Boolean
) {
    Column {
        Text("Correo electrónico", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Ingresa tu correo electrónico", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                errorIndicatorColor = ErrorRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        if (isError) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Correo inválido", color = ErrorRed, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldButton,
                disabledContainerColor = DisabledButton
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Enviar código de recuperación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStepContent(
    code: String,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    isValid: Boolean,
    isInvalid: Boolean,
    onResend: () -> Unit,
    showResendSuccess: Boolean,
    isLoading: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            repeat(5) { index ->
                val char = code.getOrNull(index)?.toString() ?: ""
                Surface(
                    modifier = Modifier.size(55.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = if (isInvalid) androidx.compose.foundation.BorderStroke(2.dp, ErrorRed) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = char, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isInvalid) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Código inválido", color = ErrorRed, fontSize = 12.sp)
            }
        }

        TextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.height(0.dp).width(0.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldButton,
                disabledContainerColor = DisabledButton
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Verificar código", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
            Text("¿No te llegó el código?", color = Color.LightGray, fontSize = 14.sp)
            TextButton(onClick = onResend, enabled = !isLoading) {
                Text("Reenviar", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }

        if (showResendSuccess) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(8.dp),
                color = SuccessGreenBg
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = SuccessGreenText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Código reenviado correctamente", color = SuccessGreenText, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordStepContent(
    newPassword: String,
    confirmPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    showMatchError: Boolean,
    showPasswordComplexityError: Boolean,
    isValid: Boolean,
    onFinish: () -> Unit,
    isLoading: Boolean
) {
    Column {
        Text("Nueva contraseña", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            placeholder = { Text("Mínimo 8 caracteres", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        if (showPasswordComplexityError) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Incluye mayúsculas, números y símbolos ($@#)", color = ErrorRed, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Confirmar contraseña", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("Repite tu contraseña", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
            isError = showMatchError,
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                errorIndicatorColor = ErrorRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        if (showMatchError) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Las contraseñas no coinciden", color = ErrorRed, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldButton,
                disabledContainerColor = DisabledButton
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Actualizar contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun StepCircle(number: String, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = if (isActive) EmeraldButton else Color.Gray
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(text = label, color = if (isActive) Color.White else Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// Colores personalizados
val DarkBlueBG = Color(0xFF0D1B2A)
val IconBoxColor = Color(0xFF718E7E)
val EmeraldButton = Color(0xFF308957)
val DisabledButton = Color(0xFF7E7E7E).copy(alpha = 0.5f)
val ErrorRed = Color(0xFFE63946)
val SuccessGreenBg = Color(0xFFD8F3DC)
val SuccessGreenText = Color(0xFF1B4332)
