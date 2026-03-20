package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.integradora_appmovil.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val viewModel: RegisterViewModel = viewModel()
    RegisterScreen(
        viewModel = viewModel,
        onBackToLogin = {},
        onRegisterSuccess = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val fullName = viewModel.fullName
    val email = viewModel.email
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val isRegistered = viewModel.isRegistered

    val isEmailValid = viewModel.isEmailValid
    val showEmailError = viewModel.showEmailError
    val isPasswordComplex = viewModel.isPasswordComplex
    val showPasswordError = viewModel.showPasswordError
    val passwordsMatch = viewModel.passwordsMatch
    val showMatchError = viewModel.showMatchError
    val isFormComplete = viewModel.isFormComplete
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Contenedor principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueBG)
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isRegistered) {
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Volver a inicio de sesión",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            // icono superior
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

            Text(
                text = "Crear nueva cuenta",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Regístrate con tus datos personales.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre completo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Nombre completo", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { viewModel.fullName = it },
                    placeholder = { Text("Ingresa tu nombre y apellidos", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = textFieldCustomColors()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // correo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Correo institucional", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.email = it },
                    placeholder = { Text("usuario@utez.edu.mx", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    isError = showEmailError,
                    singleLine = true,
                    colors = textFieldCustomColors()
                )
                if (showEmailError) {
                    ErrorText("Ingresa un correo válido (@utez.edu.mx)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Contraseña", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.password = it },
                    placeholder = { Text("Mínimo 8 caracteres", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    isError = showPasswordError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.White)
                        }
                    },
                    colors = textFieldCustomColors()
                )
                if (showPasswordError) {
                    ErrorText("Incluye mayúsculas, números y símbolos ($@#)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // confirmar contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Confirmar contraseña", color = Color.White, fontSize = 14.sp)
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    placeholder = { Text("Repite tu contraseña", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    isError = showMatchError,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.White)
                        }
                    },
                    colors = textFieldCustomColors()
                )
                if (showMatchError) {
                    ErrorText("Las contraseñas no coinciden")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // boton de registrar
            Button(
                onClick = { viewModel.onRegisterClicked() },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldButton,
                    disabledContainerColor = DisabledButton
                )
            ) {
                Text(
                    text = "Registrarse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

        } else {
            // se muestra la pantalla de exito final
            SuccessRegistrationContent(onFinish = {
                viewModel.resetRegistration()
                onRegisterSuccess()
            })
        }
    }
}

@Composable
fun SuccessRegistrationContent(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        // Icono de Éxito similar a Recuperación
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

        Text(
            text = "¡Registro exitoso!",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Ya puedes iniciar sesión con tus credenciales.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

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

@Composable
fun ErrorText(message: String) {
    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(message, color = ErrorRed, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldCustomColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.White,
    unfocusedIndicatorColor = Color.Gray,
    errorIndicatorColor = ErrorRed,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorTextColor = Color.White,
    cursorColor = Color.White,
    errorCursorColor = Color.White
)
