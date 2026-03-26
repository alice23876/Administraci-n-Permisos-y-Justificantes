package com.example.integradora_appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.integradora_appmovil.ui.theme.HeaderBlue
import com.example.integradora_appmovil.ui.theme.DarkBlueDrawer
import com.example.integradora_appmovil.ui.theme.BlueAction
import com.example.integradora_appmovil.ui.theme.InstitutionGreen
import com.example.integradora_appmovil.ui.theme.SuccessGreen
import com.example.integradora_appmovil.ui.theme.DisabledGray
import com.example.integradora_appmovil.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun SecurityGuardPreview() {
    SecurityGuardScreen(
        onLogout = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityGuardScreen(
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var folioText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isFormValid = folioText.isNotEmpty()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBlueDrawer,
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Opción Control de Acceso
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color.White) },
                    label = { Text("Control de Acceso", color = Color.White, fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

                // Opción Cerrar Sesión
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                    label = { Text("Cerrar sesión", color = Color.White) },
                    selected = false,
                    onClick = onLogout,
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
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
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.padding(4.dp), tint = HeaderBlue)
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control de Acceso",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Escanea el código QR del pase aprobado o ingresa el folio manualmente.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de Escaneo (Cámara)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Escanear pase de salida",
                            color = BlueAction,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )

                        // Área visual del Escáner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(DarkBlueDrawer, RoundedCornerShape(12.dp))
                                .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Guías del QR (Simuladas con un borde verde en las esquinas si se desea, aquí simplificado)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Apunta la cámara al código QR",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("¿No funciona el escáner?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Ingresa el folio manualmente abajo.", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo de Folio Manual
                        Text(
                            "FOLIO DEL PASE:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = folioText,
                            onValueChange = { folioText = it },
                            placeholder = { Text("EJ: PS-2026-0801", fontSize = 14.sp ) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFEEEEEE),
                                unfocusedContainerColor = Color(0xFFEEEEEE)
                            )
                        )
                        Text(
                            "El folio se encuentra debajo del QR en el pase del empleado.",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón Validar
                Button(
                    onClick = { showSuccessDialog = true },
                    enabled = isFormValid,
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        disabledContainerColor = DisabledGray
                    )
                ) {
                    Text("Validar Folio", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Diálogo de Éxito (Segunda Imagen)
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "¡Folio validado exitosamente!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            folioText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Ir a Inicio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}