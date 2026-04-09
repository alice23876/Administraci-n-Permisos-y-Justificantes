package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.viewmodel.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel,
    onBack: () -> Unit,
    onCreateUser: () -> Unit
) {
    val users = viewModel.users
    val areas = viewModel.areas
    val isLoading = viewModel.isLoading
    val successMessage = viewModel.successMessage
    
    val filteredUsers =
        if (viewModel.searchTerm.isBlank()) users.toList()
        else users.filter {
            it.nombre.contains(viewModel.searchTerm, ignoreCase = true) ||
                it.correo.contains(viewModel.searchTerm, ignoreCase = true)
        }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios", color = Color.White, fontWeight = FontWeight.SemiBold) },
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
                onClick = onCreateUser,
                containerColor = InstitutionGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(30.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Crear usuario", fontWeight = FontWeight.Bold) }
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
            OutlinedTextField(
                value = viewModel.searchTerm,
                onValueChange = { viewModel.searchTerm = it },
                placeholder = { Text("Buscar usuario...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF4F4F4),
                    unfocusedContainerColor = Color(0xFFF4F4F4)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SuccessGreen)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredUsers) { user ->
                        AdminUserListItem(
                            user = user,
                            onToggleStatus = { active -> viewModel.toggleUserStatus(user.id, active) },
                            onChangeRole = { 
                                viewModel.selectedUser = user
                                viewModel.showRoleDialog = true 
                            },
                            onAssignArea = {
                                viewModel.selectedUser = user
                                viewModel.showAreaDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (viewModel.showRoleDialog) {
        ChangeRoleDialog(
            user = viewModel.selectedUser,
            onDismiss = { viewModel.showRoleDialog = false },
            onConfirm = { newRole -> viewModel.updateRole(viewModel.selectedUser?.id ?: 0, newRole) }
        )
    }

    if (viewModel.showAreaDialog) {
        AssignAreaDialog(
            user = viewModel.selectedUser,
            areas = areas,
            onDismiss = { viewModel.showAreaDialog = false },
            onConfirm = { areaId -> viewModel.assignArea(viewModel.selectedUser?.id ?: 0, areaId) }
        )
    }

    if (successMessage.isNotEmpty()) {
        SuccessDialog(
            message = successMessage,
            onDismiss = { 
                viewModel.clearMessages()
                onBack() 
            }
        )
    }
}

@Composable
fun AdminUserListItem(
    user: AdminUserRemote,
    onToggleStatus: (Boolean) -> Unit,
    onChangeRole: () -> Unit,
    onAssignArea: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFECF0F1), shape = RoundedCornerShape(4.dp)) {
                        Text("#${user.id}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5A5A5A))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminStatusBadge(user.activo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = user.activo, onCheckedChange = onToggleStatus)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol: ${user.rol}", fontSize = 14.sp, color = Color.Gray)
                Text("Area: ${user.departamento}", fontSize = 14.sp, color = Color.Gray)
            }
            Text("Correo: ${user.correo}", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { /* Ver detalles */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver detalles", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onChangeRole,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cambiar rol", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onAssignArea,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Asignar área", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminStatusBadge(active: Boolean) {
    val label = if (active) "ACTIVO" else "INACTIVO"
    val color = if (active) SuccessGreen else Color(0xFFF26D6D)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoleDialog(
    user: AdminUserRemote?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user?.rol ?: "") }
    val roles = listOf("Director de área", "Recursos Humanos", "Guardia", "Docente")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar rol - ${user?.nombre}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text("Rol actual: ${user?.rol}", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Nuevo rol:", fontWeight = FontWeight.SemiBold)
                roles.forEach { role ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).background(if (selectedRole == role) Color(0xFFE3F2FD) else Color.Transparent).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                        Text(role, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = InstitutionGreen)
            ) { Text("Guardar cambios") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AssignAreaDialog(
    user: AdminUserRemote?,
    areas: List<com.example.integradora_appmovil.repository.AdminAreaRemote>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedAreaId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar área - ${user?.nombre}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text("Seleccionar área:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(areas) { area ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp).background(if (selectedAreaId == area.id) Color(0xFFE3F2FD) else Color.Transparent).padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedAreaId == area.id, onClick = { selectedAreaId = area.id })
                            Text(area.nombre, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedAreaId?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = InstitutionGreen),
                enabled = selectedAreaId != null
            ) { Text("Asignar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(message, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Ir a Inicio") }
            }
        },
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}
