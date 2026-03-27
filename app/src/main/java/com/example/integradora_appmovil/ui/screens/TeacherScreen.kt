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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.integradora_appmovil.ui.theme.ErrorRed
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.DarkBlueDrawer
import com.example.integradora_appmovil.ui.theme.WelcomeCardBG
import com.example.integradora_appmovil.ui.theme.ActiveGreen
import com.example.integradora_appmovil.ui.theme.BlueAction
import com.example.integradora_appmovil.ui.theme.GreenAction
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.ui.theme.DisabledGray


// --- 1. MODELOS DE DATOS ---

data class UserProfile(
    val name: String,
    val lastName: String = "",
    val email: String = "",
    val area: String = "",
    val position: String = "",
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TeacherScreenPreview() {
    TeacherScreen(
        userData = UserProfile(name = "Elena", status = "Activa"),
        onLogout = {},
        onProfileClick = {},
        onNavigateToRequests = {},
        onNavigateToNewRequest = {},
        onNavigateToNewExitPermit = {}
    )
}

// --- 3. PANTALLA DE INICIO (DOCENTE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    userData: UserProfile, 
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToNewRequest: () -> Unit,
    onNavigateToNewExitPermit: () -> Unit
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
                
                // BOTÓN MI PERFIL ACTUALIZADO
                DrawerMenuItem(icon = Icons.Default.Person, label = "Perfil", onClick = {
                    scope.launch { drawerState.close() }
                    onProfileClick()
                })
                
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
                            Text("¡Bienvenido/a ${userData.name}!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
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
                    Text("Ver historial", fontSize = 13.sp, color = Color.LightGray, modifier = Modifier.clickable { onNavigateToRequests() } )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TramiteButton(
                        modifier = Modifier.weight(1f).clickable { onNavigateToNewRequest() },
                        title = "Nuevo justificante",
                        subtitle = "Reportar inasistencia",
                        icon = Icons.Default.Description,
                        containerColor = BlueAction
                    )
                    TramiteButton(
                        modifier = Modifier.weight(1f).clickable { onNavigateToNewExitPermit() },
                        title = "Pase de Salida",
                        subtitle = "Salida anticipada",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        containerColor = GreenAction)
                }
            }
        }
    }
}

// --- 4. PANTALLA DE NUEVA SOLICITUD DE JUSTIFICANTE ---
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

            Text("Motivo de la incidencia (Mínimo 10 caracteres) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
        val calendar = Calendar.getInstance()
        val todayMillis = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -3) // ← Permite hasta 4 días antes
        val minMillis = calendar.timeInMillis
        
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Solo permitimos hoy y 4 días anteriores
                    return utcTimeMillis in minMillis..todayMillis
                }
            }
        )

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
        ) { 
            DatePicker(state = datePickerState) 
        }
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
// --- 5. NUEVA PANTALLA: NUEVO PASE DE SALIDA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExitPermitScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var motive by remember { mutableStateOf("") }

    // Estados para "Regresa el mismo día"
    var selectday by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val options = listOf("Sí", "No")

    // Estados para selectores y diálogos
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showTimeErrorDialog by remember { mutableStateOf(false) }

    // Validación de formulario
    val isFormValid = selectedDate.isNotEmpty() &&
            selectedTime.isNotEmpty() &&
            selectday.isNotEmpty() &&
            motive.length >= 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("PermiApp", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Nuevo Pase de Salida", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
            Text("Datos del trámite:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

            // Campo Fecha
            Text("Fecha de salida:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("dd/mm/aaaa") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            )

            // Campo Horario (Seleccionable, no escribible)
            Text("Horario de salida:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("--:-- ----") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora")
                    }
                },
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de regresa el mismo dia (Dropdown)
            Text("¿Regresa el mismo día?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectday,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectday = option
                                isDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Motivo
            Text("Motivo de la salida:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = motive,
                onValueChange = { motive = it },
                placeholder = { Text("Explica el motivo de tu salida...", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { showSuccessDialog = true },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        disabledContainerColor = DisabledGray
                    )
                ) {
                    Text("Enviar", color = Color.White)
                }
            }
        }
    }

    // Lógica de Calendario
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0);
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val minDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.timeInMillis
                    return utcTimeMillis in minDate..today
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        selectedDate = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Lógica de Selector de Hora (TimePicker)
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 10,
            initialMinute = 0,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute

                    // VALIDACIÓN: Solo entre 7:00 AM (7) y 8:00 PM (20)
                    if (hour < 7 || (hour > 20 || (hour == 20 && minute > 0))) {
                        showTimeErrorDialog = true
                    } else {
                        val amPm = if (hour < 12) "a.m." else "p.m."
                        val displayHour = when {
                            hour == 0 -> 12
                            hour > 12 -> hour - 12
                            else -> hour
                        }
                        selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm)
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // Diálogo de error por hora fuera de rango
    if (showTimeErrorDialog) {
        AlertDialog(
            onDismissRequest = { showTimeErrorDialog = false },
            title = { Text("Hora no permitida") },
            text = {
                Text(
                    text = "El horario de salida debe estar comprendido entre las 7:00 a.m. y las 8:00 p.m.",
                    color = ErrorRed)
            },
            confirmButton = {
                TextButton(onClick = { showTimeErrorDialog = false }) { Text("Entendido") }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¡Solicitud enviada!", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 20.dp))
                    Button(
                        onClick = { showSuccessDialog = false; onSuccess() },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Ir a Inicio", color = Color.White)
                    }
                }
            }
        }
    }
}

// Componente auxiliar para envolver el TimePicker en un diálogo
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}

// --- 6. PANTALLA DE HISTORIAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    requests: List<RequestHistory>,
    onBack: () -> Unit,
    onNewRequest: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String = "",
    onRetry: () -> Unit = {}
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

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SuccessGreen)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage, color = ErrorRed)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (requests.isEmpty()) {
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

// --- 7. PANTALLA DE PERFIL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserProfile?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("PermiApp", fontWeight = FontWeight.SemiBold, color = Color.White) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
        }
    ) { padding ->
        if (userData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) { Text("No se pudo cargar la información.", color = Color.Gray) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(text = "Mi perfil", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF424242))
                Spacer(modifier = Modifier.height(20.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE2DF)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color(0xFFE0E0E0).copy(alpha = 0.8f)) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color(0xFFBDBDBD))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(text = "${userData.name} ${userData.lastName}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF616161))
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = if (userData.status == "Activa") ActiveGreen else Color.Gray, shape = RoundedCornerShape(4.dp)) {
                                Text(text = userData.status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoField(modifier = Modifier.weight(1f), label = "Nombre:", value = userData.name)
                    InfoField(modifier = Modifier.weight(1f), label = "Área:", value = userData.area)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoField(modifier = Modifier.weight(1f), label = "Apellidos:", value = userData.lastName)
                    InfoField(modifier = Modifier.weight(1f), label = "Cargo:", value = userData.position)
                }
                Spacer(modifier = Modifier.height(24.dp))
                InfoField(modifier = Modifier.fillMaxWidth(), label = "Correo electrónico:", value = userData.email)
            }
        }
    }
}

// --- 7. COMPONENTES REUTILIZABLES ---
@Composable
fun InfoField(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF616161))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value.ifEmpty { "Sin asignar" }, fontSize = 15.sp, color = Color(0xFF9E9E9E))
    }
}

@Composable
fun HistoryItemCard(item: RequestHistory) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFECF0F1), shape = RoundedCornerShape(4.dp)) { Text(text = item.id, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(color = item.statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, item.statusColor)) {
                    Text(text = item.status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = item.statusColor)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Fecha", fontSize = 11.sp, color = Color.Gray); Text(item.date, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                if (item.status == "Aprobado") Icon(Icons.Default.QrCode, contentDescription = null, tint = BlueAction, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TramiteButton(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector, containerColor: Color) {
    Card(modifier = modifier.height(140.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}
