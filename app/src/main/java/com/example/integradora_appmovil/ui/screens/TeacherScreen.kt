package com.example.integradora_appmovil.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.util.*

//  MODELOS DE DATOS
data class UserProfile(
    val name: String,
    val status: String,
    val imageUrl: String? = null
)

data class RequestHistory(
    val id: String,
    val type: String,
    val date: String,
    val status: String,
    val statusColor: Color
)

// PALETA DE COLORES
val HeaderBlue = Color(0xFF2C3E50)
val DarkBlueDrawer = Color(0xFF212E3D)
val WelcomeCardBG = Color(0xFFDDE5E0)
val ActiveGreen = Color(0xFF94D1A1)
val BlueAction = Color(0xFF2B78E4)
val GreenAction = Color(0xFF76C887)
val InstitutionGreen = Color(0xFF718E7E)
val SuccessGreen = Color(0xFF2ECC71)
val DisabledGray = Color(0xFFBDC3C7)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TeacherScreenPreview() {
    TeacherScreen(
        userData = UserProfile(name = "Elena", status = "Activa"),
        onLogout = {},
        onProfileClick = {},
        onNavigateToRequests = {}
    )
}

// PANTALLA DE INICIO DOCENTE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    userData: UserProfile, 
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToRequests: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBlueDrawer,
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                DrawerMenuItem(icon = Icons.Default.Home, label = "Inicio", isSelected = true, onClick = { scope.launch { drawerState.close() } })
                DrawerMenuItem(icon = Icons.Default.History, label = "Solicitudes", onClick = onNavigateToRequests)
                DrawerMenuItem(icon = Icons.Default.Person, label = "Perfil", onClick = onProfileClick)
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                DrawerMenuItem(icon = Icons.Default.Lock, label = "Cerrar sesión", onClick = onLogout)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("PermiApp", fontWeight = FontWeight.SemiBold, color = Color.White) } },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White) } },
                    actions = {
                        Surface(modifier = Modifier.padding(end = 12.dp).size(32.dp), shape = RoundedCornerShape(4.dp), color = InstitutionGreen) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.padding(4.dp), tint = HeaderBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(20.dp)) {
                Text("Inicio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = WelcomeCardBG), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.LightGray.copy(alpha = 0.5f)) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(10.dp), tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("¡Bienvenida ${userData.name}!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (userData.status == "Activa") ActiveGreen else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = userData.status,
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("Trámites", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
                    Text("Ver historial", fontSize = 13.sp, color = Color.LightGray, modifier = Modifier.clickable { onNavigateToRequests() })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TramiteButton(modifier = Modifier.weight(1f), title = "Nuevo justificante", subtitle = "Reportar inasistencia", icon = Icons.Default.Description, containerColor = BlueAction)
                    TramiteButton(modifier = Modifier.weight(1f), title = "Pase de Salida", subtitle = "Salida anticipada", icon = Icons.AutoMirrored.Filled.ExitToApp, containerColor = GreenAction)
                }
            }
        }
    }
}

// PANTALLA DE NUEVA SOLICITUD
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("") }
    var motive by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedPdfUri = uri }

    val isFormValid = selectedDate.isNotEmpty() && motive.length >= 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("PermiApp", fontWeight = FontWeight.Bold, color = Color.White) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(20.dp)) {
            Text("Nuevo justificante", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
            Text("Datos de la incidencia:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

            Text("Fecha de la incidencia:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("dd/mm/aaaa") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = null) } },
                shape = RoundedCornerShape(8.dp)
            )

            Text("Motivo de la incidencia (Mínimo 10) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = motive,
                onValueChange = { motive = it },
                placeholder = { Text("Explica los detalles...", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Text("Comprobante (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(80.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { pdfPickerLauncher.launch("application/pdf") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (selectedPdfUri == null) Icons.Default.FileUpload else Icons.Default.PictureAsPdf, contentDescription = null, tint = if (selectedPdfUri == null) Color.Gray else SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (selectedPdfUri == null) "Adjuntar PDF" else "Archivo seleccionado", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(45.dp), shape = RoundedCornerShape(4.dp)) { Text("Cancelar") }
                Button(
                    onClick = { showSuccessDialog = true },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, disabledContainerColor = DisabledGray)
                ) { Text("Enviar", color = Color.White) }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it
                        selectedDate = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Solicitud enviada!", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 20.dp))
                    Button(onClick = { showSuccessDialog = false; onSuccess() }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) { Text("Ir a Inicio") }
                }
            }
        }
    }
}

// --- 5. PANTALLA DE HISTORIAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    requests: List<RequestHistory>,
    onBack: () -> Unit,
    onNewRequest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewRequest,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA)).padding(16.dp)) {
            Text(text = "Historial de solicitudes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

            if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes solicitudes registradas", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(requests) { item -> HistoryItemCard(item) }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: RequestHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFECF0F1), shape = RoundedCornerShape(4.dp)) {
                        Text(text = item.id, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(
                    color = item.statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, item.statusColor)
                ) {
                    Text(text = item.status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = item.statusColor)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Fecha", fontSize = 11.sp, color = Color.Gray)
                    Text(item.date, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                if (item.status == "Aprobado") Icon(Icons.Default.QrCode, contentDescription = null, tint = BlueAction, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// --- 6. COMPONENTES REUTILIZABLES ---
@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TramiteButton(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector, containerColor: Color) {
    Card(modifier = modifier.height(140.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}
