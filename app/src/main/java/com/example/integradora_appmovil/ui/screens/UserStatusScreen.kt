package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.viewmodel.UserStatusViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val statusDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun normalizeStatusValue(value: String): String =
    value.lowercase()
        .normalize()

private fun String.normalize(): String = java.text.Normalizer.normalize(this.trim(), java.text.Normalizer.Form.NFD)
    .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

private fun formatStatusDate(value: String): String {
    if (value.isBlank()) {
        return "--/--/----"
    }

    val instant = runCatching { Instant.parse(value) }.getOrNull() ?: return "--/--/----"
    return instant.atZone(ZoneId.systemDefault()).toLocalDate().format(statusDateFormatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatusScreen(
    viewModel: UserStatusViewModel,
    onBack: () -> Unit
) {
    val users = viewModel.users
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val panelUsers = users.filterNot { normalizeStatusValue(it.rol).contains("super") }

    val filteredUsers =
        if (viewModel.searchTerm.isBlank()) panelUsers
        else panelUsers.filter {
            it.nombre.contains(viewModel.searchTerm, ignoreCase = true) ||
                it.correo.contains(viewModel.searchTerm, ignoreCase = true)
        }

    val activeUsers = filteredUsers.filter { it.activo }
    val inactiveUsers = filteredUsers.filter { !it.activo }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estados de usuario", color = Color.White, fontWeight = FontWeight.SemiBold) },
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

            when {
                isLoading && users.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SuccessGreen)
                }
                errorMessage.isNotEmpty() && users.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color(0xFFE53935))
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (activeUsers.isNotEmpty()) {
                        item { Text("Usuarios activos.", fontWeight = FontWeight.Bold, color = Color.Gray) }
                        items(activeUsers) { user ->
                            UserStatusListItem(
                                user = user,
                                onToggleStatus = { nextActive ->
                                    viewModel.toggleUserStatus(user.id, nextActive)
                                }
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
                            UserStatusListItem(
                                user = user,
                                onToggleStatus = { nextActive ->
                                    viewModel.toggleUserStatus(user.id, nextActive)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserStatusListItem(
    user: AdminUserRemote,
    onToggleStatus: (Boolean) -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5A5A5A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user.correo, fontSize = 14.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminStatusBadge(user.activo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = user.activo, onCheckedChange = onToggleStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Área: ${user.departamento.ifBlank { "-" }}", fontSize = 14.sp, color = Color.Gray)
                Text("Rol: ${user.rol.ifBlank { "-" }}", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Última fecha de cambio: ${formatStatusDate(user.fechaEstado)}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
        }
    }
}
