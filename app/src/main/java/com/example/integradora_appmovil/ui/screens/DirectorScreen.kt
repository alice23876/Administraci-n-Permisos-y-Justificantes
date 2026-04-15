package com.example.integradora_appmovil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.integradora_appmovil.model.AuthSession
import com.example.integradora_appmovil.ui.theme.*
import com.example.integradora_appmovil.util.PdfFileHandler
import com.example.integradora_appmovil.viewmodel.DirectorViewModel
import kotlinx.coroutines.launch

// Enum para gestionar la navegación interna del Director
enum class DirectorNav {
    HOME, PENDING_REQUESTS, REQUEST_DETAIL, HISTORY, PROFILE
}

// Modelo de datos para las solicitudes (preparado para DB)
data class RequestItem(
    val id: String,
    val teacherName: String,
    val type: String, // "Justificante" o "Permiso"
    val area: String = "",
    val date: String,
    val status: String, // "PENDIENTE", "APROBADO", "RECHAZADO"
    val reason: String = "Asuntos personales / Médicos",
    val approvedBy: String = "Sin asignar",
    val requestedDate: String = "",
    val requestedTime: String = "",
    val departureRegisteredAt: String = "",
    val entryRegisteredAt: String = "",
    val returnsSameDay: Boolean? = null,
    val hasPdf: Boolean = false,
    val attachmentName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorScreen(
    viewModel: DirectorViewModel,
    session: AuthSession?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(DirectorNav.HOME) }
    val pendingRequests = viewModel.pendingRequests
    val historyRequests = viewModel.historyRequests
    val selectedRequest = viewModel.selectedRequest
    val isLoading = viewModel.isLoading
    val isDetailLoading = viewModel.isDetailLoading
    val errorMessage = viewModel.errorMessage
    val detailErrorMessage = viewModel.detailErrorMessage

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBlueDrawer,
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                DrawerMenuDirectorItem(icon = Icons.Default.Home, label = "Inicio", isSelected = currentScreen == DirectorNav.HOME, onClick = {
                    currentScreen = DirectorNav.HOME
                    scope.launch { drawerState.close() }
                })
                DrawerMenuDirectorItem(icon = Icons.Default.PendingActions, label = "Solicitudes pendientes", isSelected = currentScreen == DirectorNav.PENDING_REQUESTS, onClick = {
                    currentScreen = DirectorNav.PENDING_REQUESTS
                    scope.launch { drawerState.close() }
                })
                DrawerMenuDirectorItem(icon = Icons.Default.Person, label = "Perfil", isSelected = currentScreen == DirectorNav.PROFILE, onClick = {
                    currentScreen = DirectorNav.PROFILE
                    scope.launch { drawerState.close() }
                })

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                DrawerMenuDirectorItem (icon = Icons.Default.Lock, label = "Cerrar sesión", onClick = onLogout)
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
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        Surface(
                            modifier = Modifier.padding(end = 12.dp).size(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = InstitutionGreen
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.padding(4.dp), tint = HeaderBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
                when (currentScreen) {
                    DirectorNav.HOME -> DirectorHome(
                        session = session,
                        requests = pendingRequests + historyRequests,
                        isLoading = isLoading,
                        onPendingClick = { currentScreen = DirectorNav.PENDING_REQUESTS },
                        onHistoryClick = { currentScreen = DirectorNav.HISTORY }
                    )
                    DirectorNav.PENDING_REQUESTS -> PendingRequestsList(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        requests = pendingRequests,
                        onRetry = { viewModel.refreshRequests() },
                        onDetailClick = {
                            viewModel.selectRequest(it)
                            currentScreen = DirectorNav.REQUEST_DETAIL
                        }
                    )
                    DirectorNav.REQUEST_DETAIL -> selectedRequest?.let {
                        RequestDetailView(
                            isLoading = isDetailLoading,
                            errorMessage = detailErrorMessage,
                            request = it,
                            onDownloadAttachment = {
                                viewModel.downloadSelectedAttachment(
                                    onSuccess = { file ->
                                        PdfFileHandler.openPdf(context, file.fileName, file.bytes, file.contentType)
                                    },
                                    onError = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onBack = {
                                viewModel.clearSelectedRequest()
                                currentScreen = DirectorNav.PENDING_REQUESTS
                            },
                            onAction = { status ->
                                viewModel.updateRequestStatus(it.id, status) {
                                    currentScreen = DirectorNav.PENDING_REQUESTS
                                }
                            }
                        )
                    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isDetailLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(detailErrorMessage.ifBlank { "No se pudo cargar la solicitud" }, color = Color.Gray)
                        }
                    }
                    DirectorNav.HISTORY -> HistoryView(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        requests = historyRequests,
                        onBack = { currentScreen = DirectorNav.HOME },
                        onRetry = { viewModel.refreshRequests() }
                    )
                    DirectorNav.PROFILE -> DirectorProfile(
                        session = session,
                        requests = pendingRequests + historyRequests,
                        onBack = { currentScreen = DirectorNav.HOME }
                    )
                }
            }
        }
    }
}

@Composable
fun DirectorHome(
    session: AuthSession?,
    requests: List<RequestItem>,
    isLoading: Boolean,
    onPendingClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val displayName = session?.nombre?.ifBlank { "Director" } ?: "Director"
    val areaName = requests.firstOrNull { it.area.isNotBlank() }?.area ?: "Sin área asignada"

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
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
                    Text("¡Bienvenido/a $displayName!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(areaName, color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ActiveGreen,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Activo",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = SuccessGreen
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Gestión", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
            Text("Ver historial", fontSize = 13.sp, color = Color.LightGray, modifier = Modifier.clickable { onHistoryClick() } )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DirectorActionButton(
                modifier = Modifier.weight(1f).clickable { onPendingClick() },
                title = "Solicitudes",
                subtitle = "Pendientes hoy",
                icon = Icons.Default.HourglassEmpty,
                containerColor = BlueAction
            )
            DirectorActionButton(
                modifier = Modifier.weight(1f).clickable { onHistoryClick() },
                title = "Historial",
                subtitle = "Ver registros",
                icon = Icons.Default.History,
                containerColor = GreenAction
            )
        }
    }
}

@Composable
fun DirectorProfile(
    session: AuthSession?,
    requests: List<RequestItem>,
    onBack: () -> Unit
) {
    val displayName = session?.nombre?.ifBlank { "Director" } ?: "Director"
    val areaName = requests.firstOrNull { it.area.isNotBlank() }?.area ?: "Sin área asignada"

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BlueAction)
            Text("Regresar", color = BlueAction, modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Mi perfil", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WelcomeCardBG),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(10.dp))
                Text(session?.correo ?: "", color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(session?.rol ?: "Director de area", color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(areaName, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        ChangePasswordSection(session = session)
    }
}

@Composable
fun DirectorActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
                Icon(icon, contentDescription = null, modifier = Modifier
                    .size(70.dp)
                    .padding(8.dp),
                    tint = Color.White
                )            }
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PendingRequestsList(
    requests: List<RequestItem>,
    isLoading: Boolean,
    errorMessage: String,
    onRetry: () -> Unit,
    onDetailClick: (RequestItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Solicitudes Pendientes", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage, color = ErrorRed, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay solicitudes pendientes", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(requests) { request ->
                    RequestCard(request, onDetailClick)
                }
            }
        }
    }
}

@Composable
fun RequestCard(request: RequestItem, onDetailClick: (RequestItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {Text(request.teacherName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusBadge(request.status)
            }
            Text("Tipo: ${request.type}", fontSize = 14.sp, color = Color.Gray)
            Text("Área: ${request.area}", fontSize = 14.sp, color = Color.Gray)
            Text("Fecha: ${request.date}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onDetailClick(request) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = BlueAction),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Ver detalle", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RequestDetailView(
    request: RequestItem,
    isLoading: Boolean,
    errorMessage: String,
    onDownloadAttachment: () -> Unit,
    onBack: () -> Unit,
    onAction: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BlueAction)
            Text("Regresar", color = BlueAction, modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Detalle de la Solicitud", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("ID: #${request.id}", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        DetailField("Docente", request.teacherName)
        DetailField("Tipo de solicitud", request.type)
        DetailField("Área", request.area)
        DetailField("Motivo", request.reason)
        DetailField("Fecha solicitada", request.requestedDate.ifBlank { request.date })
        if (request.requestedTime.isNotBlank()) {
            DetailField("Hora solicitada", request.requestedTime)
        }
        DetailField("Aprobado por", request.approvedBy)
        request.returnsSameDay?.let {
            DetailField("¿Regresa el mismo día?", if (it) "Sí" else "No")
        }
        if (request.departureRegisteredAt.isNotBlank()) {
            DetailField("Fecha de salida registrada", request.departureRegisteredAt)
        }
        if (request.entryRegisteredAt.isNotBlank()) {
            DetailField("Fecha de entrada registrada", request.entryRegisteredAt)
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = ErrorRed, modifier = Modifier.padding(bottom = 12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (request.type == "Justificante") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (request.attachmentName.isNotBlank()) request.attachmentName else "Comprobante adjunto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text("Toca descargar para abrir el PDF", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(
                        onClick = onDownloadAttachment
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = BlueAction)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    Text("Código QR de salida", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onAction("RECHAZADO") },
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Text("Rechazar")
            }
            Button(
                onClick = { onAction("APROBADO") },
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Aprobar")
                }
            }
        }

        TextButton(
            onClick = { onBack() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Cancelar proceso", color = Color.Gray)
        }
    }
}

@Composable
fun HistoryView(
    requests: List<RequestItem>,
    isLoading: Boolean,
    errorMessage: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BlueAction)
            Text("Regresar al inicio", color = BlueAction, modifier = Modifier.padding(start = 4.dp))
        }
        Text("Historial de Solicitudes", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(vertical = 16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
        } else if (errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage, color = ErrorRed, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay registros en el historial", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(requests) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(request.teacherName, fontWeight = FontWeight.Bold)
                                Text("${request.type} - ${request.area} - ${request.date}", fontSize = 13.sp, color = Color.Gray)
                            }
                            StatusBadge(request.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "APROBADO" -> SuccessGreen
        "RECHAZADO" -> ErrorRed
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            status,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun DrawerMenuDirectorItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)) },
        label = { Text(label, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            selectedContainerColor = Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
