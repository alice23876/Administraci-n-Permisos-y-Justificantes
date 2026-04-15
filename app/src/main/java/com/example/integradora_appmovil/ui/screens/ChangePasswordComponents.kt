package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.network.ApiException
import com.example.integradora_appmovil.repository.UserRepository
import com.example.integradora_appmovil.ui.theme.ErrorRed
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.ui.theme.WelcomeCardBG
import kotlinx.coroutines.launch

private fun canChangePassword(session: AuthSession?): Boolean =
    !session?.correo.equals("admin", ignoreCase = true)

private fun isValidChangePassword(value: String): Boolean =
    value.length >= 8 &&
        value.any(Char::isUpperCase) &&
        value.any(Char::isDigit) &&
        value.any { it == '$' || it == '@' || it == '#' }

@Composable
fun ChangePasswordSection(
    session: AuthSession?,
    modifier: Modifier = Modifier
) {
    if (!canChangePassword(session)) {
        return
    }

    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WelcomeCardBG),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Actualiza tu contraseña para mantener segura tu cuenta.",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { showDialog = true }) {
                Text("Cambiar contraseña")
            }
        }
    }

    ChangePasswordDialog(
        session = session,
        isOpen = showDialog,
        onDismiss = { showDialog = false }
    )
}

@Composable
fun ChangePasswordDialog(
    session: AuthSession?,
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen || !canChangePassword(session) || session == null) {
        return
    }

    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    var currentPassword by remember(isOpen) { mutableStateOf("") }
    var newPassword by remember(isOpen) { mutableStateOf("") }
    var confirmPassword by remember(isOpen) { mutableStateOf("") }
    var isSaving by remember(isOpen) { mutableStateOf(false) }
    var errorMessage by remember(isOpen) { mutableStateOf("") }
    var successMessage by remember(isOpen) { mutableStateOf("") }

    val canSave = currentPassword.isNotBlank() &&
        isValidChangePassword(newPassword) &&
        confirmPassword == newPassword &&
        confirmPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Mínimo 8 caracteres con mayúscula, número y símbolo ($@#).",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotBlank()) {
                    Text(errorMessage, color = ErrorRed, fontSize = 13.sp)
                }

                if (successMessage.isNotBlank()) {
                    Text(successMessage, color = SuccessGreen, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!canSave || isSaving) {
                        return@Button
                    }

                    scope.launch {
                        isSaving = true
                        errorMessage = ""
                        successMessage = ""
                        try {
                            val message = repository.changeAuthenticatedPassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword,
                                token = session.token
                            )
                            successMessage = message
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } catch (exception: ApiException) {
                            errorMessage = exception.message ?: "No se pudo actualizar la contraseña"
                        } catch (_: Exception) {
                            errorMessage = "No se pudo conectar con el servidor"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = canSave && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cerrar")
            }
        }
    )
}
