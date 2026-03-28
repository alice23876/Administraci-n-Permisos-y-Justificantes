package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.repository.AdminAreaRemote
import com.example.integradora_appmovil.repository.AdminUserRemote
import com.example.integradora_appmovil.ui.theme.ActiveGreen
import com.example.integradora_appmovil.ui.theme.DarkBlueDrawer
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.ui.theme.WelcomeCardBG
import com.example.integradora_appmovil.viewmodel.SuperAdminViewModel
import kotlinx.coroutines.launch

enum class SuperAdminNav {
    HOME, USERS, AREAS, STATUSES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: SuperAdminViewModel,
    session: AuthSession?,
    onLogout: () -> Unit
) {
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(SuperAdminNav.HOME) }
    var searchTerm by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<AdminUserRemote?>(null) }

    val users = viewModel.users
    val areas = viewModel.areas
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val updatingUserId = viewModel.updatingUserId

    LaunchedEffect(session?.correo, session?.token) {
        viewModel.bindSession(session)
    }

    val filteredUsers = remember(users, searchTerm) {
        if (searchTerm.isBlank()) {
            users.toList()
        } else {
            val query = searchTerm.trim().lowercase()
            users.filter {
                it.nombre.lowercase().contains(query) ||
                        it.correo.lowercase().contains(query) ||
                        it.rol.lowercase().contains(query) ||
                        it.departamento.lowercase().contains(query)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBlueDrawer,
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                SuperAdminDrawerItem(Icons.Default.Home, "Inicio", currentScreen == SuperAdminNav.HOME) {
                    currentScreen = SuperAdminNav.HOME
                    scope.launch { drawerState.close() }
                }
                SuperAdminDrawerItem(Icons.Default.Group, "Usuarios", currentScreen == SuperAdminNav.USERS) {
                    currentScreen = SuperAdminNav.USERS
                    scope.launch { drawerState.close() }
                }
                SuperAdminDrawerItem(Icons.Default.History, "Áreas", currentScreen == SuperAdminNav.AREAS) {
                    currentScreen = SuperAdminNav.AREAS
                    scope.launch { drawerState.close() }
                }
                SuperAdminDrawerItem(Icons.Default.ToggleOn, "Estados de usuario", currentScreen == SuperAdminNav.STATUSES) {
                    currentScreen = SuperAdminNav.STATUSES
                    scope.launch { drawerState.close() }
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                SuperAdminDrawerItem(Icons.Default.Lock, "Cerrar sesión", false, onClick = onLogout)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("PermiApp", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        Surface(
                            modifier = Modifier.padding(end = 12.dp).size(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = InstitutionGreen
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.padding(4.dp), tint = HeaderBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                when (currentScreen) {
                    SuperAdminNav.HOME -> {
                        SuperAdminHome(
                            session = session,
                            userCount = users.size,
                            areaCount = areas.size,
                            onUsersClick = { currentScreen = SuperAdminNav.USERS },
                            onAreasClick = { currentScreen = SuperAdminNav.AREAS },
                            onStatusesClick = { currentScreen = SuperAdminNav.STATUSES }
                        )
                    }

                    SuperAdminNav.USERS -> {
                        SuperAdminUsersView(
                            searchTerm = searchTerm,
                            onSearchChange = { searchTerm = it },
                            users = filteredUsers,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            updatingUserId = updatingUserId,
                            onRetry = { viewModel.refreshAll() },
                            onToggleStatus = { user, activo -> viewModel.updateUserStatus(user, activo) },
                            onViewDetails = { selectedUser = it }
                        )
                    }

                    SuperAdminNav.AREAS -> {
                        SuperAdminAreasView(
                            areas = areas.toList(),
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onRetry = { viewModel.refreshAll() }
                        )
                    }

                    SuperAdminNav.STATUSES -> {
                        SuperAdminStatusesView(
                            users = users.toList(),
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            updatingUserId = updatingUserId,
                            onRetry = { viewModel.refreshAll() },
                            onToggleStatus = { user, activo -> viewModel.updateUserStatus(user, activo) },
                            onViewDetails = { selectedUser = it }
                        )
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        AdminUserDetailDialog(
            user = user,
            onDismiss = { selectedUser = null }
        )
    }
}

@Composable
private fun SuperAdminHome(
    session: AuthSession?,
    userCount: Int,
    areaCount: Int,
    onUsersClick: () -> Unit,
    onAreasClick: () -> Unit,
    onStatusesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Inicio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WelcomeCardBG),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.5f)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(10.dp), tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(session?.nombre?.ifBlank { "SuperAdmin" } ?: "SuperAdmin", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = ActiveGreen, shape = RoundedCornerShape(4.dp)) {
                        Text("Activo", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text("Acciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SuperAdminActionCard(
                modifier = Modifier.weight(1f),
                title = "Usuarios",
                subtitle = "Gestionar usuarios",
                icon = Icons.Default.Group,
                containerColor = Color(0xFF2D74DA),
                onClick = onUsersClick
            )
            SuperAdminActionCard(
                modifier = Modifier.weight(1f),
                title = "Áreas",
                subtitle = "$areaCount áreas registradas",
                icon = Icons.Default.History,
                containerColor = Color(0xFF57B2A4),
                onClick = onAreasClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SuperAdminActionCard(
            modifier = Modifier.fillMaxWidth(0.45f),
            title = "Estados de usuario",
            subtitle = "$userCount usuarios en sistema",
            icon = Icons.Default.ToggleOn,
            containerColor = Color(0xFF7694BF),
            onClick = onStatusesClick
        )
    }
}

@Composable
private fun SuperAdminUsersView(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    users: List<AdminUserRemote>,
    isLoading: Boolean,
    errorMessage: String,
    updatingUserId: Long?,
    onRetry: () -> Unit,
    onToggleStatus: (AdminUserRemote, Boolean) -> Unit,
    onViewDetails: (AdminUserRemote) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Usuarios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchTerm,
            onValueChange = onSearchChange,
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
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
            errorMessage.isNotEmpty() -> AdminErrorState(errorMessage, onRetry)
            users.isEmpty() -> AdminEmptyState("Sin usuarios registrados")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(users) { user ->
                    AdminUserCard(
                        user = user,
                        isUpdating = updatingUserId == user.id,
                        showToggle = true,
                        onToggleStatus = onToggleStatus,
                        onViewDetails = onViewDetails
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperAdminAreasView(
    areas: List<AdminAreaRemote>,
    isLoading: Boolean,
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Áreas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
            errorMessage.isNotEmpty() -> AdminErrorState(errorMessage, onRetry)
            areas.isEmpty() -> AdminEmptyState("Sin registro de áreas")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(areas) { area ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(area.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5A5A5A))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Director: ${area.director.ifBlank { "Sin asignar" }}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperAdminStatusesView(
    users: List<AdminUserRemote>,
    isLoading: Boolean,
    errorMessage: String,
    updatingUserId: Long?,
    onRetry: () -> Unit,
    onToggleStatus: (AdminUserRemote, Boolean) -> Unit,
    onViewDetails: (AdminUserRemote) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Estados de usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
            errorMessage.isNotEmpty() -> AdminErrorState(errorMessage, onRetry)
            users.isEmpty() -> AdminEmptyState("Sin usuarios registrados")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(users) { user ->
                    AdminUserCard(
                        user = user,
                        isUpdating = updatingUserId == user.id,
                        showToggle = true,
                        onToggleStatus = onToggleStatus,
                        onViewDetails = onViewDetails
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUserRemote,
    isUpdating: Boolean,
    showToggle: Boolean,
    onToggleStatus: (AdminUserRemote, Boolean) -> Unit,
    onViewDetails: (AdminUserRemote) -> Unit
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
                    Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF5A5A5A))
                }
                if (showToggle) {
                    if (user.correo.equals("admin", ignoreCase = true)) {
                        StatusBadge(user.activo)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(user.activo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = user.activo,
                                onCheckedChange = { onToggleStatus(user, it) },
                                enabled = !isUpdating
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol: ${user.rol.ifBlank { "Sin rol" }}", fontSize = 15.sp, color = Color.Gray)
                Text("Área: ${user.departamento.ifBlank { "Sin asignar" }}", fontSize = 15.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Correo:", fontSize = 14.sp, color = Color.Gray)
            Text(user.correo, fontSize = 15.sp, color = Color(0xFF6A6A6A))
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onViewDetails(user) },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color(0xFF6B6B6B)
                )
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver detalles", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AdminUserDetailDialog(
    user: AdminUserRemote,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Usuario #${user.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                AdminDetailRow("Nombre:", user.nombre)
                Spacer(modifier = Modifier.height(10.dp))
                AdminDetailRow("Correo:", user.correo)
                Spacer(modifier = Modifier.height(10.dp))
                AdminDetailRow("Rol:", user.rol.ifBlank { "Sin rol" })
                Spacer(modifier = Modifier.height(10.dp))
                AdminDetailRow("Área:", user.departamento.ifBlank { "Sin asignar" })
                Spacer(modifier = Modifier.height(10.dp))
                AdminDetailRow("Estado:", if (user.activo) "Activo" else "Inactivo")
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDetailRow(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = Color(0xFF4A4A4A), fontSize = 15.sp)
    }
}

@Composable
private fun StatusBadge(active: Boolean) {
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

@Composable
private fun SuperAdminActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(145.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun AdminErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Color(0xFFE53935))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun AdminEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray)
    }
}

@Composable
private fun SuperAdminDrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
