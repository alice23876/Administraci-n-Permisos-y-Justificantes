package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.viewmodel.CreateUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(
    viewModel: CreateUserViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val roles = listOf("Director de área", "Recursos Humanos", "Guardia", "Docente")
    var expandedRoles by remember { mutableStateOf(false) }
    var expandedAreas by remember { mutableStateOf(false) }
    val areas = viewModel.availableAreas.value

    LaunchedEffect(Unit) {
        viewModel.loadAreas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo usuario", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Datos de registro:", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(label = "Nombre completo:", value = viewModel.nombreCompleto, onValueChange = { viewModel.nombreCompleto = it }, placeholder = "Luis Eduardo Ramirez Hernandez")
            AdminTextField(label = "Correo electrónico:", value = viewModel.correo, onValueChange = { viewModel.correo = it }, placeholder = "luisramirez@utez.edu.mx")
            AdminTextField(label = "Contraseña:", value = viewModel.password, onValueChange = { viewModel.password = it }, placeholder = "**********", isPassword = true)
            AdminTextField(label = "Verificar contraseña:", value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, placeholder = "**********", isPassword = true)

            Text("Rol:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedRoles,
                onExpandedChange = { expandedRoles = !expandedRoles }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedRol,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoles) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F4F4),
                        unfocusedContainerColor = Color(0xFFF4F4F4)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedRoles,
                    onDismissRequest = { expandedRoles = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                viewModel.selectedRol = role
                                expandedRoles = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Área:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedAreas,
                onExpandedChange = { expandedAreas = !expandedAreas }
            ) {
                OutlinedTextField(
                    value = areas.find { it.id == viewModel.selectedAreaId }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar área") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAreas) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F4F4),
                        unfocusedContainerColor = Color(0xFFF4F4F4)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedAreas,
                    onDismissRequest = { expandedAreas = false }
                ) {
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area.nombre) },
                            onClick = {
                                viewModel.selectedAreaId = area.id
                                expandedAreas = false
                            }
                        )
                    }
                }
            }

            if (viewModel.errorMessage.isNotEmpty()) {
                Text(viewModel.errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) { Text("Cancelar") }

                Button(
                    onClick = { viewModel.createUser(onSuccess) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InstitutionGreen),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Crear usuario")
                }
            }
        }
    }

    if (viewModel.successMessage.isNotEmpty()) {
        SuccessDialog(message = viewModel.successMessage, onDismiss = onSuccess)
    }
}

@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F4F4),
                unfocusedContainerColor = Color(0xFFF4F4F4)
            )
        )
    }
}
