package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.viewmodel.UserStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatusScreen(
    viewModel: UserStatusViewModel,
    onBack: () -> Unit
) {
    val users = viewModel.users
    val isLoading = viewModel.isLoading
    
    val filteredUsers = remember(users, viewModel.searchTerm) {
        if (viewModel.searchTerm.isBlank()) users.toList()
        else users.filter { 
            it.nombre.contains(viewModel.searchTerm, ignoreCase = true) || 
            it.correo.contains(viewModel.searchTerm, ignoreCase = true) 
        }
    }

    val activeUsers = filteredUsers.filter { it.activo }
    val inactiveUsers = filteredUsers.filter { !it.activo }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estados de usuario", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (activeUsers.isNotEmpty()) {
                        item { Text("Usuarios activos.", fontWeight = FontWeight.Bold, color = Color.Gray) }
                        items(activeUsers) { user ->
                            AdminUserListItem(
                                user = user,
                                onToggleStatus = { active -> viewModel.toggleUserStatus(user.id, active) },
                                onChangeRole = {}, // No necesario en esta pantalla según el diseño
                                onAssignArea = {}
                            )
                        }
                    }

                    if (inactiveUsers.isNotEmpty()) {
                        item { 
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Usuarios inactivos.", fontWeight = FontWeight.Bold, color = Color.Gray) 
                        }
                        items(inactiveUsers) { user ->
                            AdminUserListItem(
                                user = user,
                                onToggleStatus = { active -> viewModel.toggleUserStatus(user.id, active) },
                                onChangeRole = {},
                                onAssignArea = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
