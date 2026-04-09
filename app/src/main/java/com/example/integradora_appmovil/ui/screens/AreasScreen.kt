package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.repository.AdminAreaRemote
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.viewmodel.AreasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreasScreen(
    viewModel: AreasViewModel,
    onBack: () -> Unit,
    onCreateArea: () -> Unit
) {
    val areas = viewModel.areas
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage

    LaunchedEffect(Unit) {
        viewModel.loadAreas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Áreas registradas", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateArea,
                containerColor = InstitutionGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(30.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva área", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            if (isLoading && areas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SuccessGreen)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color.Red)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(areas) { area ->
                        AdminAreaListItem(
                            area = area,
                            onAssignDirector = {
                                viewModel.selectedArea = area
                                viewModel.showAssignDirectorDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (viewModel.showAssignDirectorDialog) {
        AssignDirectorDialog(
            area = viewModel.selectedArea,
            directors = viewModel.availableDirectors,
            onDismiss = { viewModel.showAssignDirectorDialog = false },
            onConfirm = { directorId -> viewModel.assignDirector(viewModel.selectedArea?.id ?: 0, directorId) }
        )
    }

    if (successMessage.isNotEmpty()) {
        SuccessDialog(
            message = successMessage,
            onDismiss = { viewModel.clearMessages() }
        )
    }
}

@Composable
fun AdminAreaListItem(
    area: AdminAreaRemote,
    onAssignDirector: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFECF0F1), shape = RoundedCornerShape(4.dp)) {
                    Text("#${area.id}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(area.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5A5A5A))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Director: ${area.director.ifBlank { "Sin asignar" }}", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAssignDirector,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color(0xFF6B6B6B)
                )
            ) {
                Text("Asignar director", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignDirectorDialog(
    area: AdminAreaRemote?,
    directors: List<com.example.integradora_appmovil.repository.AdminUserRemote>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedDirectorId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar director - ${area?.nombre}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text("Seleccionar usuario:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = directors.find { it.id == selectedDirectorId }?.let { "${it.nombre} - ${it.rol}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar usuario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F4F4),
                            unfocusedContainerColor = Color(0xFFF4F4F4)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        directors.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.nombre} - ${user.rol}") },
                                onClick = {
                                    selectedDirectorId = user.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDirectorId?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = InstitutionGreen),
                enabled = selectedDirectorId != null
            ) { Text("Asignar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}
