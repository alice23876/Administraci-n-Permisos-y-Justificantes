package com.example.integradora_appmovil.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class SuperAdminNav {
    HOME, USERS, AREAS, STATUSES
}

private const val ADMIN_STATUS_DATES_STORAGE_KEY = "superAdminStatusDates"
private val adminStatusDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private val availableRoles = listOf(
    "Super administrador",
    "Director de area",
    "Recursos humanos",
    "Guardia",
    "Docente"
)

private val availableCreateRoles = listOf(
    "Super administrador",
    "Recursos humanos",
    "Guardia"
)

private fun normalizeValue(value: String): String =
    value.lowercase()
        .normalize()

private fun String.normalize(): String = java.text.Normalizer.normalize(this.trim(), java.text.Normalizer.Form.NFD)
    .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

private fun validateAdminName(value: String) = value.trim().length >= 5
private fun validateAdminEmail(value: String) =
    Regex("^[^\\s@]+@(?:[a-z0-9-]+\\.)*[a-z0-9-]+\\.edu\\.mx$", RegexOption.IGNORE_CASE)
        .matches(value.trim())
private fun validateAdminPassword(value: String) =
    value.length >= 8 && value.any(Char::isUpperCase) && value.any(Char::isDigit) && value.any { it == '$' || it == '@' || it == '#' }
private fun validateAreaName(value: String) =
    value.trim().length >= 2 && value.trim().length <= 100 && Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]+$").matches(value.trim())

private fun isMutedDepartment(value: String): Boolean {
    val normalized = normalizeValue(value)
    return normalized == "administrador" || normalized == "guardia de caseta" || normalized == "rh"
}

private fun loadAdminStatusDates(context: Context): Map<Long, String> {
    val rawValue = context.getSharedPreferences("permiapp_admin", Context.MODE_PRIVATE)
        .getString(ADMIN_STATUS_DATES_STORAGE_KEY, null)
        ?: return emptyMap()

    val parsed = mutableMapOf<Long, String>()
    val jsonObject = runCatching { org.json.JSONObject(rawValue) }.getOrNull() ?: return emptyMap()
    val keys = jsonObject.keys()

    while (keys.hasNext()) {
        val key = keys.next()
        val userId = key.toLongOrNull() ?: continue
        val value = jsonObject.optString(key)
        if (value.isNotBlank()) {
            parsed[userId] = value
        }
    }

    return parsed
}

private fun persistAdminStatusDates(context: Context, statusDates: Map<Long, String>) {
    val jsonObject = org.json.JSONObject()
    statusDates.forEach { (userId, value) ->
        jsonObject.put(userId.toString(), value)
    }

    context.getSharedPreferences("permiapp_admin", Context.MODE_PRIVATE)
        .edit()
        .putString(ADMIN_STATUS_DATES_STORAGE_KEY, jsonObject.toString())
        .apply()
}

private fun formatAdminStatusDate(value: String): String {
    if (value.isBlank()) {
        return "--/--/----"
    }

    val instant = runCatching { Instant.parse(value) }.getOrNull() ?: return "--/--/----"
    return instant.atZone(ZoneId.systemDefault()).toLocalDate().format(adminStatusDateFormatter)
}

private fun buildDefaultAdminUser(session: AuthSession?): AdminUserRemote {
    return AdminUserRemote(
        id = -1L,
        nombre = session?.nombre?.ifBlank { "Administrador" } ?: "Administrador",
        correo = session?.correo?.ifBlank { "admin" } ?: "admin",
        departamento = "Administrador",
        rol = "Super administrador",
        activo = true
    )
}

private fun buildPanelUsers(
    users: List<AdminUserRemote>,
    session: AuthSession?
): List<AdminUserRemote> {
    val shouldShowDefaultAdmin =
        normalizeValue(session?.rol.orEmpty()).contains("super") &&
            users.none { it.correo.equals("admin", ignoreCase = true) }

    return if (shouldShowDefaultAdmin) {
        listOf(buildDefaultAdminUser(session)) + users
    } else {
        users
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: SuperAdminViewModel,
    session: AuthSession?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val statusDatesByUserId = remember { androidx.compose.runtime.mutableStateMapOf<Long, String>() }
    var currentScreen by remember { mutableStateOf(SuperAdminNav.HOME) }
    var searchTerm by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<AdminUserRemote?>(null) }
    var createUserOpen by remember { mutableStateOf(false) }
    var createAreaOpen by remember { mutableStateOf(false) }
    var editingUserRole by remember { mutableStateOf<AdminUserRemote?>(null) }
    var editingUserArea by remember { mutableStateOf<AdminUserRemote?>(null) }
    var editingAreaDirector by remember { mutableStateOf<AdminAreaRemote?>(null) }
    var selectedRole by remember { mutableStateOf("") }
    var selectedAreaId by remember { mutableStateOf<Long?>(null) }
    var selectedDirectorId by remember { mutableStateOf<Long?>(null) }
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserPassword by remember { mutableStateOf("") }
    var newUserConfirmPassword by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("") }
    var newAreaName by remember { mutableStateOf("") }

    val users = viewModel.users
    val areas = viewModel.areas
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val updatingUserId = viewModel.updatingUserId
    val savingArea = viewModel.savingArea
    val savingUser = viewModel.savingUser
    val successMessage = viewModel.successMessage

    LaunchedEffect(session?.correo, session?.token) {
        statusDatesByUserId.clear()
        statusDatesByUserId.putAll(loadAdminStatusDates(context))
        viewModel.bindSession(session)
    }

    val panelUsers = buildPanelUsers(users.toList(), session)
        .map { user -> user.copy(fechaEstado = statusDatesByUserId[user.id].orEmpty()) }
    val normalizedSearchTerm = searchTerm.trim().lowercase()
    val filteredUsers = if (normalizedSearchTerm.isBlank()) {
        panelUsers
    } else {
        panelUsers.filter {
            it.nombre.lowercase().contains(normalizedSearchTerm) ||
                it.correo.lowercase().contains(normalizedSearchTerm) ||
                it.rol.lowercase().contains(normalizedSearchTerm) ||
                it.departamento.lowercase().contains(normalizedSearchTerm)
        }
    }

    val roleOptions = remember(editingUserRole) {
        availableRoles.filter { normalizeValue(it) != normalizeValue(editingUserRole?.rol ?: "") }
    }
    val areaOptions = areas.filter { area ->
        normalizeValue(area.nombre) != normalizeValue(editingUserArea?.departamento ?: "")
    }
    val directorOptions = users.filter { user ->
        !user.correo.equals("admin", ignoreCase = true) &&
            normalizeValue(user.nombre) != normalizeValue(editingAreaDirector?.director ?: "")
    }
    val canCreateUser = validateAdminName(newUserName) &&
        validateAdminEmail(newUserEmail) &&
        validateAdminPassword(newUserPassword) &&
        newUserConfirmPassword == newUserPassword &&
        newUserConfirmPassword.isNotBlank() &&
        newUserRole.isNotBlank()
    val canCreateArea = validateAreaName(newAreaName) &&
        areas.none { normalizeValue(it.nombre) == normalizeValue(newAreaName) }
    val canSaveRole = editingUserRole != null &&
        selectedRole.isNotBlank() &&
        normalizeValue(selectedRole) != normalizeValue(editingUserRole?.rol ?: "")
    val currentAreaId =
        areas.firstOrNull { normalizeValue(it.nombre) == normalizeValue(editingUserArea?.departamento ?: "") }?.id
    val currentDirectorId =
        users.firstOrNull { normalizeValue(it.nombre) == normalizeValue(editingAreaDirector?.director ?: "") }?.id
    val canSaveArea = editingUserArea != null && selectedAreaId != null && selectedAreaId != currentAreaId
    val canSaveDirector = editingAreaDirector != null && selectedDirectorId != null && selectedDirectorId != currentDirectorId
    val statusPanelUsers = panelUsers.filterNot { normalizeValue(it.rol).contains("super") }

    fun toggleUserStatusWithDate(user: AdminUserRemote, activo: Boolean) {
        viewModel.updateUserStatus(user, activo)
        statusDatesByUserId[user.id] = Instant.now().toString()
        persistAdminStatusDates(context, statusDatesByUserId.toMap())
    }

    fun closeUserDialogs() {
        createUserOpen = false
        editingUserRole = null
        editingUserArea = null
        selectedRole = ""
        selectedAreaId = null
        newUserName = ""
        newUserEmail = ""
        newUserPassword = ""
        newUserConfirmPassword = ""
        newUserRole = ""
    }

    fun closeAreaDialogs() {
        createAreaOpen = false
        editingAreaDirector = null
        selectedDirectorId = null
        newAreaName = ""
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
                            onToggleStatus = { user, activo -> toggleUserStatusWithDate(user, activo) },
                            onViewDetails = { selectedUser = it },
                            onCreateUser = { createUserOpen = true },
                            onChangeRole = {
                                editingUserRole = it
                                selectedRole = it.rol
                            },
                            onAssignArea = {
                                editingUserArea = it
                                selectedAreaId = areas.firstOrNull { area -> normalizeValue(area.nombre) == normalizeValue(it.departamento) }?.id
                            },
                            currentUserCorreo = session?.correo.orEmpty()
                        )
                    }

                    SuperAdminNav.AREAS -> {
                        SuperAdminAreasView(
                            users = users.toList(),
                            areas = areas.toList(),
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onRetry = { viewModel.refreshAll() },
                            onCreateArea = { createAreaOpen = true },
                            onAssignDirector = {
                                editingAreaDirector = it
                                selectedDirectorId = users.firstOrNull { user -> normalizeValue(user.nombre) == normalizeValue(it.director) }?.id
                            }
                        )
                    }

                    SuperAdminNav.STATUSES -> {
                        SuperAdminStatusesView(
                            users = statusPanelUsers,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            updatingUserId = updatingUserId,
                            onRetry = { viewModel.refreshAll() },
                            onToggleStatus = { user, activo -> toggleUserStatusWithDate(user, activo) },
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

    if (createUserOpen) {
        AdminCreateUserDialog(
            nombre = newUserName,
            correo = newUserEmail,
            password = newUserPassword,
            confirmPassword = newUserConfirmPassword,
            role = newUserRole,
            saving = savingUser,
            canSave = canCreateUser,
            onNameChange = { newUserName = it },
            onEmailChange = { newUserEmail = it },
            onPasswordChange = { newUserPassword = it },
            onConfirmPasswordChange = { newUserConfirmPassword = it },
            onRoleChange = { newUserRole = it },
            onDismiss = { closeUserDialogs() },
            onSave = {
                viewModel.createUser(
                    nombre = newUserName.trim(),
                    correo = newUserEmail.trim(),
                    contraseña = newUserPassword,
                    rol = newUserRole
                ) { closeUserDialogs() }
            }
        )
    }

    editingUserRole?.let { user ->
        AdminChangeRoleDialog(
            user = user,
            roleOptions = roleOptions,
            selectedRole = selectedRole,
            saving = updatingUserId == user.id,
            canSave = canSaveRole,
            onRoleChange = { selectedRole = it },
            onDismiss = { closeUserDialogs() },
            onSave = {
                viewModel.updateUserRole(user.id, selectedRole) { closeUserDialogs() }
            }
        )
    }

    editingUserArea?.let { user ->
        AdminAssignAreaDialog(
            user = user,
            areas = areaOptions,
            selectedAreaId = selectedAreaId,
            saving = updatingUserId == user.id,
            canSave = canSaveArea,
            onAreaChange = { selectedAreaId = it },
            onDismiss = { closeUserDialogs() },
            onSave = {
                selectedAreaId?.let { areaId ->
                    viewModel.updateUserArea(user.id, areaId) { closeUserDialogs() }
                }
            }
        )
    }

    if (createAreaOpen) {
        AdminCreateAreaDialog(
            areaName = newAreaName,
            saving = savingArea,
            canSave = canCreateArea,
            onAreaNameChange = { newAreaName = it },
            onDismiss = { closeAreaDialogs() },
            onSave = {
                viewModel.createArea(newAreaName.trim()) { closeAreaDialogs() }
            }
        )
    }

    editingAreaDirector?.let { area ->
        AdminAssignDirectorDialog(
            area = area,
            users = directorOptions,
            selectedDirectorId = selectedDirectorId,
            saving = savingArea,
            canSave = canSaveDirector,
            onDirectorChange = { selectedDirectorId = it },
            onDismiss = { closeAreaDialogs() },
            onSave = {
                selectedDirectorId?.let { userId ->
                    viewModel.assignDirector(area.id, userId) { closeAreaDialogs() }
                }
            }
        )
    }

    if (successMessage.isNotBlank()) {
        AdminSuccessDialog(
            message = successMessage,
            onDismiss = {
                viewModel.clearMessages()
                currentScreen = SuperAdminNav.HOME
            }
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
    onViewDetails: (AdminUserRemote) -> Unit,
    onCreateUser: () -> Unit,
    onChangeRole: (AdminUserRemote) -> Unit,
    onAssignArea: (AdminUserRemote) -> Unit,
    currentUserCorreo: String
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = onCreateUser,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear usuario")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
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
                        onViewDetails = onViewDetails,
                        onChangeRole = onChangeRole,
                        onAssignArea = onAssignArea,
                        currentUserCorreo = currentUserCorreo
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperAdminAreasView(
    users: List<AdminUserRemote>,
    areas: List<AdminAreaRemote>,
    isLoading: Boolean,
    errorMessage: String,
    onRetry: () -> Unit,
    onCreateArea: () -> Unit,
    onAssignDirector: (AdminAreaRemote) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Áreas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = onCreateArea,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva área")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
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
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onAssignDirector(area) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF0F0F0),
                                    contentColor = Color(0xFF6B6B6B)
                                )
                            ) {
                                Text(if (area.director.isBlank()) "Asignar director" else "Cambiar director", fontWeight = FontWeight.SemiBold)
                            }
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
                    AdminUserStatusCard(
                        user = user,
                        isUpdating = updatingUserId == user.id,
                        onToggleStatus = onToggleStatus,
                        onViewDetails = onViewDetails
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserStatusCard(
    user: AdminUserRemote,
    isUpdating: Boolean,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5A5A5A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user.correo, fontSize = 14.sp, color = Color.Gray)
                }
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(user.activo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = user.activo,
                        onCheckedChange = { onToggleStatus(user, it) },
                        enabled = !isUpdating
                    )
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
                    text = "Última fecha de cambio: ${formatAdminStatusDate(user.fechaEstado)}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
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
private fun AdminUserCard(
    user: AdminUserRemote,
    isUpdating: Boolean,
    showToggle: Boolean,
    onToggleStatus: (AdminUserRemote, Boolean) -> Unit,
    onViewDetails: (AdminUserRemote) -> Unit,
    onChangeRole: (AdminUserRemote) -> Unit = {},
    onAssignArea: (AdminUserRemote) -> Unit = {},
    currentUserCorreo: String = ""
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = Color(0xFFECF0F1), shape = RoundedCornerShape(4.dp)) {
                        Text("#${user.id}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.nombre,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF5A5A5A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showToggle) {
                    if (user.correo.equals("admin", ignoreCase = true)) {
                        StatusBadge(user.activo)
                    } else {
                        Row(
                            modifier = Modifier.padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                Text(
                    "Área: ${user.departamento.ifBlank { "Sin asignar" }}",
                    fontSize = 15.sp,
                    color = if (isMutedDepartment(user.departamento)) Color(0xFF9B9B9B) else Color.Gray
                )
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
            val canEdit = !user.correo.equals("admin", ignoreCase = true) && !user.correo.equals(currentUserCorreo, ignoreCase = true)
            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onChangeRole(user) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color(0xFF6B6B6B))
                    ) {
                        Text("Cambiar rol", fontSize = 12.sp)
                    }
                    if (normalizeValue(user.rol).contains("docente")) {
                        Button(
                            onClick = { onAssignArea(user) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color(0xFF6B6B6B))
                        ) {
                            Text(if (user.departamento.isBlank()) "Asignar área" else "Cambiar área", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminChangeRoleDialog(
    user: AdminUserRemote,
    roleOptions: List<String>,
    selectedRole: String,
    saving: Boolean,
    canSave: Boolean,
    onRoleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cambiar rol - ${user.nombre}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                Text("Rol actual:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(user.rol, fontSize = 15.sp, color = Color(0xFF4A4A4A))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nuevo rol:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecciona un rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F4F4),
                            unfocusedContainerColor = Color(0xFFF4F4F4)
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text(user.rol) }, onClick = {}, enabled = false)
                        roleOptions.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    onRoleChange(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = canSave && !saving,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text(if (saving) "Guardando..." else "Guardar cambios")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAssignAreaDialog(
    user: AdminUserRemote,
    areas: List<AdminAreaRemote>,
    selectedAreaId: Long?,
    saving: Boolean,
    canSave: Boolean,
    onAreaChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${if (user.departamento.isBlank()) "Asignar área" else "Cambiar área"} - ${user.nombre}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                Text("Seleccionar área:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = areas.firstOrNull { it.id == selectedAreaId }?.nombre.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(if (areas.isEmpty()) "Sin áreas registradas" else "Selecciona un área") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = areas.isNotEmpty()),
                        enabled = areas.isNotEmpty(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F4F4),
                            unfocusedContainerColor = Color(0xFFF4F4F4)
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = { Text(area.nombre) },
                                onClick = {
                                    onAreaChange(area.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = canSave && !saving, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                        Text(if (saving) "Asignando..." else "Asignar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCreateUserDialog(
    nombre: String,
    correo: String,
    password: String,
    confirmPassword: String,
    role: String,
    saving: Boolean,
    canSave: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Crear usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                OutlinedTextField(value = nombre, onValueChange = onNameChange, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = correo, onValueChange = onEmailChange, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = onConfirmPasswordChange, label = { Text("Verificar contraseña") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableCreateRoles.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = {
                                onRoleChange(item)
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = canSave && !saving, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                        Text(if (saving) "Creando..." else "Crear usuario")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCreateAreaDialog(
    areaName: String,
    saving: Boolean,
    canSave: Boolean,
    onAreaNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Nueva área", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                OutlinedTextField(
                    value = areaName,
                    onValueChange = { next ->
                        val sanitized = next.replace(Regex("[^A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]"), "").take(100)
                        onAreaNameChange(sanitized)
                    },
                    label = { Text("Nombre del área") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = canSave && !saving, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                        Text(if (saving) "Creando..." else "Crear área")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAssignDirectorDialog(
    area: AdminAreaRemote,
    users: List<AdminUserRemote>,
    selectedDirectorId: Long?,
    saving: Boolean,
    canSave: Boolean,
    onDirectorChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${if (area.director.isBlank()) "Asignar director" else "Cambiar director"} - ${area.nombre.lowercase()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))
                Text("Seleccionar usuario:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = users.firstOrNull { it.id == selectedDirectorId }?.let { "${it.nombre} - ${it.rol}" }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar usuario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (area.director.isNotBlank()) {
                            DropdownMenuItem(text = { Text("${area.director} - Director de área") }, onClick = {}, enabled = false)
                        }
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.nombre} - ${user.rol}") },
                                onClick = {
                                    onDirectorChange(user.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = canSave && !saving, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                        Text(if (saving) "Asignando..." else "Asignar")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF5A5A5A))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                    Text("Ir a Inicio")
                }
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
