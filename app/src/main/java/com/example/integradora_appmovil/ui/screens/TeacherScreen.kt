package com.example.integradora_appmovil.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
import com.example.integradora_appmovil.util.PdfFileHandler
import com.example.integradora_appmovil.model.AuthSession


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
    val numericId: Long,
    val id: String,
    val type: String,
    val directivo: String,
    val area: String,
    val date: String,
    val requestDate: String,
    val status: String,
    val statusColor: Color,
    val canViewQr: Boolean
)

data class TeacherRequestDetailState(
    val id: Long,
    val tipo: String,
    val directivo: String,
    val area: String,
    val fecha: String,
    val estado: String,
    val motivo: String,
    val fechaSolicitada: String,
    val horaSolicitada: String,
    val fechaSalidaRegistrada: String,
    val fechaEntradaRegistrada: String,
    val regresaMismoDia: Boolean?,
    val tieneComprobante: Boolean,
    val comprobanteNombre: String
)

data class TeacherQrState(
    val solicitudId: Long,
    val qrValue: String,
    val folio: String,
    val aprobadoPor: String,
    val validoPara: String,
    val usosRestantes: Int
)

private val teacherDisplayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun isBusinessDay(date: LocalDate): Boolean =
    date.dayOfWeek.value !in setOf(6, 7)

private fun getAllowedBusinessDates(baseDate: LocalDate = LocalDate.now()): List<LocalDate> {
    val allowedDates = mutableListOf<LocalDate>()
    var cursor = baseDate

    while (allowedDates.size < 3) {
        if (isBusinessDay(cursor)) {
            allowedDates += cursor
        }
        cursor = cursor.minusDays(1)
    }

    return allowedDates.sorted()
}

private fun toDatePickerUtcMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun fromDatePickerUtcMillis(utcTimeMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()

private fun formatTeacherDisplayDate(date: LocalDate): String =
    date.format(teacherDisplayDateFormatter)

private fun isTeacherQrCutoffReached(currentTime: LocalTime = LocalTime.now()): Boolean =
    !currentTime.isBefore(LocalTime.of(21, 0))

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TeacherScreenPreview() {
    TeacherScreen(
        userData = UserProfile(name = "Elena", status = "Activa"),
        hasTodayExitPermit = false,
        hasTodayJustificante = false,
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
    hasTodayExitPermit: Boolean = false,
    hasTodayJustificante: Boolean = false,
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
                        modifier = Modifier.weight(1f),
                        title = "Nuevo justificante",
                        subtitle = "Reportar inasistencia",
                        icon = Icons.Default.Description,
                        containerColor = BlueAction,
                        onClick = onNavigateToNewRequest
                    )
                    TramiteButton(
                        modifier = Modifier.weight(1f),
                        title = "Pase de Salida",
                        subtitle = "Salida anticipada",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        containerColor = GreenAction,
                        onClick = onNavigateToNewExitPermit
                    )
                }
            }
        }
    }
}

// --- 4. PANTALLA DE NUEVA SOLICITUD DE JUSTIFICANTE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    blockedJustificanteDates: Set<String>,
    isSubmitting: Boolean,
    submitError: String,
    onSubmit: (String, String, Uri?, () -> Unit) -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val allowedDates = remember { getAllowedBusinessDates() }
    val allowedDateKeys = remember(allowedDates) { allowedDates.map(LocalDate::toString).toSet() }
    var selectedDate by remember { mutableStateOf("") }
    var selectedDateValue by remember { mutableStateOf("") }
    var motive by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedPdfUri = uri }
    val isSelectedDateBlocked = selectedDateValue.isNotEmpty() && blockedJustificanteDates.contains(selectedDateValue)

    val isFormValid =
        selectedDate.isNotEmpty() &&
            selectedDateValue.isNotEmpty() &&
            motive.trim().length >= 100 &&
            !isSelectedDateBlocked &&
            !isSubmitting

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

            Text("Motivo de la incidencia (Mínimo 100 caracteres) *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                    onClick = {
                        onSubmit(
                            selectedDateValue,
                            motive.trim(),
                            selectedPdfUri
                        ) {
                            showSuccessDialog = true
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, disabledContainerColor = DisabledGray)
                ) { Text("Enviar", color = Color.White) }
            }

            if (isSelectedDateBlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ya solicitaste un justificante para esa fecha válida. Solo puedes pedir uno por día de incidencia.",
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            } else if (submitError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = submitError,
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = allowedDates.lastOrNull()?.let(::toDatePickerUtcMillis),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selectedLocalDate = fromDatePickerUtcMillis(utcTimeMillis)
                    return selectedLocalDate.toString() in allowedDateKeys
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedLocalDate = fromDatePickerUtcMillis(it)
                        if (selectedLocalDate.toString() in allowedDateKeys) {
                            selectedDate = formatTeacherDisplayDate(selectedLocalDate)
                            selectedDateValue = selectedLocalDate.toString()
                        }
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
    hasTodayExitPermit: Boolean,
    isSubmitting: Boolean,
    submitError: String,
    onSubmit: (String, Boolean, String, () -> Unit) -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val currentDate = remember { formatTeacherDisplayDate(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf("") }
    var selectedTimeValue by remember { mutableStateOf("") }
    var motive by remember { mutableStateOf("") }

    // Estados para "Regresa el mismo día"
    var selectday by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val options = listOf("Sí", "No")

    // Estados para selectores y diálogos
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showTimeErrorDialog by remember { mutableStateOf(false) }

    // Validación de formulario
    val isFormValid = selectedTime.isNotEmpty() &&
            selectday.isNotEmpty() &&
            motive.trim().length >= 100 &&
            !hasTodayExitPermit &&
            !isSubmitting

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
                value = currentDate,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
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
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
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
            Text("Motivo de la salida (Mínimo 100 caracteres):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                    onClick = {
                        onSubmit(
                            selectedTimeValue,
                            selectday == "Sí",
                            motive.trim()
                        ) {
                            showSuccessDialog = true
                        }
                    },
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

            if (hasTodayExitPermit) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ya solicitaste un pase de salida hoy. Solo puedes pedir uno por día.",
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            } else if (submitError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = submitError,
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            }
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
                        selectedTimeValue = String.format("%02d:%02d", hour, minute)
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
    onRetry: () -> Unit = {},
    onViewDetail: (RequestHistory) -> Unit = {},
    onViewQr: (RequestHistory) -> Unit = {},
    selectedDetail: TeacherRequestDetailState? = null,
    isDetailLoading: Boolean = false,
    detailError: String = "",
    onDismissDetail: () -> Unit = {},
    selectedQr: TeacherQrState? = null,
    isQrLoading: Boolean = false,
    qrError: String = "",
    onDismissQr: () -> Unit = {},
    onDownloadAttachment: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
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
                    items(requests) { item ->
                        HistoryItemCard(
                            item = item,
                            onViewDetail = { onViewDetail(item) },
                            onViewQr = { onViewQr(item) }
                        )
                    }
                }
            }
        }
    }

    if (selectedDetail != null || isDetailLoading || detailError.isNotEmpty()) {
        TeacherRequestDetailDialog(
            request = selectedDetail,
            isLoading = isDetailLoading,
            errorMessage = detailError,
            onDismiss = onDismissDetail,
            onDownloadAttachment = onDownloadAttachment
        )
    }

    if (selectedQr != null || isQrLoading || qrError.isNotEmpty()) {
        TeacherQrDialog(
            qrState = selectedQr,
            isLoading = isQrLoading,
            errorMessage = qrError,
            onDismiss = onDismissQr
        )
    }
}

@Composable
fun TeacherRequestDetailDialog(
    request: TeacherRequestDetailState?,
    isLoading: Boolean,
    errorMessage: String,
    onDismiss: () -> Unit,
    onDownloadAttachment: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    Text(
                        text = request?.let { "${it.tipo} #${it.id}" } ?: "Detalle de solicitud",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))

                when {
                    isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SuccessGreen)
                    }
                    errorMessage.isNotEmpty() -> Text(errorMessage, color = ErrorRed)
                    request != null -> {
                        val isPermiso = request.tipo.contains("permiso", ignoreCase = true)

                        DetailRow("Directivo:", request.directivo.ifBlank { "Sin asignar" }, "Area:", request.area.ifBlank { "Sin asignar" })
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Tipo:", request.tipo, "Fecha:", request.fecha)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            if (isPermiso) "Hora solicitada:" else "Fecha solicitada:",
                            if (isPermiso) request.horaSolicitada.ifBlank { "--:--" } else request.fechaSolicitada.ifBlank { "--/--/----" },
                            "Estado:",
                            request.estado
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (isPermiso) {
                            DetailRow(
                                "Regresa mismo dia?",
                                if (request.regresaMismoDia == true) "Si" else "No",
                                "Salida registrada:",
                                request.fechaSalidaRegistrada.ifBlank { "--/--/---- --:--" }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(
                                "Entrada registrada:",
                                request.fechaEntradaRegistrada.ifBlank { "--/--/---- --:--" },
                                "",
                                ""
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text("Motivo:", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(request.motivo, color = Color(0xFF4A4A4A), fontSize = 15.sp)

                        if (request.tieneComprobante) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Color(0xFFE8F8EC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF7EDB8C)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Comprobante adjunto", color = Color(0xFF4A9E59), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        request.comprobanteNombre.ifBlank { "comprobante.pdf" },
                                        color = Color(0xFF4A4A4A),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = onDownloadAttachment,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Descargar")
                                    }
                                }
                            }
                        }
                    }
                }

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
fun TeacherQrDialog(
    qrState: TeacherQrState?,
    isLoading: Boolean,
    errorMessage: String,
    onDismiss: () -> Unit
) {
    val qrCutoffReached = isTeacherQrCutoffReached()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Codigo QR — Pase de Salida", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE4E4E4))

                when {
                    isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SuccessGreen)
                    }
                    errorMessage.isNotEmpty() -> Text(errorMessage, color = ErrorRed)
                    qrCutoffReached -> Text(
                        "El QR deja de estar disponible después de las 09:00pm.",
                        color = ErrorRed
                    )
                    qrState != null -> {
                        Surface(
                            color = Color(0xFFDFF3E3),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF7EDB8C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pase aprobado por ${qrState.aprobadoPor}", color = Color(0xFF4A9E59), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(qrState.validoPara, color = Color(0xFF4A9E59), fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(2.dp, Color(0xFFC4C4C4)),
                            modifier = Modifier.width(240.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = "https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${Uri.encode(qrState.qrValue)}",
                                    contentDescription = "QR del permiso",
                                    modifier = Modifier.size(180.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFC4C4C4))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Folio del pase", color = Color.Gray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(qrState.folio, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(leftLabel: String, leftValue: String, rightLabel: String, rightValue: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            if (leftLabel.isNotBlank()) {
                Text(leftLabel, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(leftValue.ifBlank { "Sin asignar" }, color = Color(0xFF4A4A4A), fontSize = 15.sp)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            if (rightLabel.isNotBlank()) {
                Text(rightLabel, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(rightValue.ifBlank { "Sin asignar" }, color = Color(0xFF4A4A4A), fontSize = 15.sp)
            }
        }
    }
}

// --- 7. PANTALLA DE PERFIL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserProfile?,
    session: AuthSession?,
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
                Spacer(modifier = Modifier.height(24.dp))
                ChangePasswordSection(session = session)
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
fun HistoryItemCard(
    item: RequestHistory,
    onViewDetail: () -> Unit,
    onViewQr: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Directivo: ${item.directivo.ifBlank { "Sin asignar" }}", fontSize = 14.sp, color = Color(0xFF6B6B6B))
                }
                Text("Area: ${item.area.ifBlank { "Sin asignar" }}", fontSize = 14.sp, color = Color(0xFF6B6B6B))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fecha: ${item.date}", fontSize = 14.sp, color = Color(0xFF6B6B6B))
                }
                Text("Detalles automáticos", fontSize = 12.sp, color = Color.LightGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F0F0),
                        contentColor = Color(0xFF6B6B6B)
                    )
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver detalle", fontWeight = FontWeight.SemiBold)
                }
                if (item.canViewQr && !isTeacherQrCutoffReached()) {
                    Button(
                        onClick = onViewQr,
                        modifier = Modifier.width(46.dp).height(42.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Ver QR", modifier = Modifier.size(22.dp))
                    }
                }
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
fun TramiteButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) containerColor else DisabledGray
        )
    ) {
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
