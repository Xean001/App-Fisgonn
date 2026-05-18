package com.example.fisgon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fisgon.presentation.map.MapLibreMap
import com.example.fisgon.presentation.panic.PanicViewModel
import kotlin.math.abs
import kotlin.math.roundToLong

private fun Double.fmt4(): String {
    val s = (this * 10000.0).roundToLong()
    return "${s / 10000}.${abs(s % 10000).toString().padStart(4, '0')}"
}

private val BgColor     = Color(0xFF090E1C)
private val Teal        = Color(0xFF00C9A0)
private val BorderColor = Color(0xFF1E2D47)
private val InputBg     = Color(0xFF111827)
private val CardBg      = Color(0xFF0F1929)
private val TextMuted   = Color(0xFF5A6A85)
private val RedAlert    = Color(0xFFFF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    panicViewModel: PanicViewModel,
    onOpenUrl: (String) -> Unit = {}
) {
    val state      by viewModel.uiState.collectAsState()
    val panicState by panicViewModel.uiState.collectAsState()

    // ── Diálogos de pánico ──────────────────────────────────────
    if (panicState.panicSentConfirmation) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissPanicConfirmation() },
            containerColor = Color(0xFF0A2000),
            title = { Text("ALERTA SOS ENVIADA", color = Color(0xFF4CAF50),
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp) },
            text = { Text("Tu alerta de pánico fue enviada a los usuarios cercanos.",
                color = Color.White, fontSize = 14.sp) },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissPanicConfirmation() }) {
                Text("OK", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) } }
        )
    }

    if (panicState.noLocationError) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissNoLocationError() },
            containerColor = Color(0xFF1A1000),
            title = { Text("SIN UBICACIÓN", color = Color(0xFFFF9800),
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) },
            text = { Text("Esperando señal GPS. Activa la ubicación e intenta de nuevo.",
                color = Color.White, fontSize = 14.sp) },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissNoLocationError() }) {
                Text("OK", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold) } }
        )
    }

    panicState.incomingAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissAlert() },
            containerColor = Color(0xFF1A0000),
            title = { Text("ALERTA SOS CERCANA", color = RedAlert,
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 2.sp) },
            text = {
                Column {
                    Text("Se ha recibido una alerta de pánico en tu zona.", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Ubicación: ${alert.latitude.fmt4()}, ${alert.longitude.fmt4()}",
                        color = Color(0xFFAAAAAA), fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissAlert() }) {
                Text("ENTENDIDO", color = RedAlert, fontWeight = FontWeight.Bold) } }
        )
    }

    if (state.markerAdded) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMarkerAdded() },
            containerColor = Color(0xFF0A1520),
            title = { Text("INCIDENTE MARCADO", color = RedAlert,
                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Tu reporte fue marcado en el mapa con un radio de 5 km.",
                        color = Color.White, fontSize = 14.sp)
                    if (state.reportError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("(Sin conexión — guardado solo localmente)", color = TextMuted, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.dismissMarkerAdded() }) {
                Text("OK", color = Teal, fontWeight = FontWeight.Bold) } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .safeContentPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("FISGÓN", color = Teal, fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                    Text("SEGURIDAD ACTIVA", color = TextMuted, fontSize = 8.sp, letterSpacing = 2.sp)
                }
                Text(
                    viewModel.currentUser.anonymousUsername
                        .takeIf { it.isNotBlank() }?.let { "@$it" }
                        ?: viewModel.currentUser.nombre,
                    color = TextMuted, fontSize = 11.sp
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(8.dp).background(
                        if (panicState.isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        CircleShape
                    )
                )
            }

            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
            Spacer(Modifier.height(10.dp))

            // ── Etiqueta mapa + coords ───────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("MI UBICACIÓN", color = Teal, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f))
                panicState.currentLocation?.let { loc ->
                    Text("${loc.latitude.fmt4()}, ${loc.longitude.fmt4()}",
                        color = TextMuted, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(6.dp))

            // ── MAPA GRANDE ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0A1520), RoundedCornerShape(14.dp))
                    .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                MapLibreMap(
                    modifier  = Modifier.fillMaxSize(),
                    latitude  = panicState.currentLocation?.latitude,
                    longitude = panicState.currentLocation?.longitude,
                    markers   = state.markers
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Formulario de incidente ──────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, RedAlert.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("REPORTAR INCIDENTE", color = RedAlert, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f))
                    if (state.markers.isNotEmpty()) {
                        Text("${state.markers.size} reportado${if (state.markers.size > 1) "s" else ""}",
                            color = TextMuted, fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ── Dropdown de categoría ────────────────────────
                ExposedDropdownMenuBox(
                    expanded = state.categoryMenuExpanded,
                    onExpandedChange = { viewModel.onCategoryMenuToggle() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Categoría del incidente", color = TextMuted, fontSize = 13.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.categoryMenuExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAlert, unfocusedBorderColor = BorderColor,
                            focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = state.categoryMenuExpanded,
                        onDismissRequest = { viewModel.onCategoryMenuDismiss() },
                        modifier = Modifier.background(Color(0xFF0F1929))
                    ) {
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, color = Color.White, fontSize = 13.sp) },
                                onClick = { viewModel.onCategorySelected(cat) },
                                leadingIcon = {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .background(
                                                parseHexColor(cat.color),
                                                CircleShape
                                            )
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Título ───────────────────────────────────────
                OutlinedTextField(
                    value = state.formTitle,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Título — ¿Qué está pasando?", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAlert, unfocusedBorderColor = BorderColor,
                        focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        cursorColor = RedAlert
                    )
                )
                Spacer(Modifier.height(8.dp))

                // ── Descripción ──────────────────────────────────
                OutlinedTextField(
                    value = state.formDescription,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    placeholder = { Text("Descripción del incidente...", color = TextMuted, fontSize = 13.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAlert, unfocusedBorderColor = BorderColor,
                        focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        cursorColor = RedAlert
                    )
                )

                if (state.noGpsError) {
                    Spacer(Modifier.height(4.dp))
                    Text("Sin señal GPS. Activa la ubicación.", color = Color(0xFFFF9800), fontSize = 11.sp)
                }

                Spacer(Modifier.height(10.dp))

                // ── Botón marcar ─────────────────────────────────
                Button(
                    onClick = {
                        viewModel.onMarkLocation(
                            panicState.currentLocation?.latitude,
                            panicState.currentLocation?.longitude
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedAlert.copy(alpha = 0.18f),
                        contentColor = RedAlert
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedAlert.copy(alpha = 0.6f))
                ) {
                    Text("Marcar mi ubicación en el mapa",
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(90.dp))
        }

        // ── Botón SOS ────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxSize().safeContentPadding().padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { panicViewModel.onPanicButtonPressed() },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (panicState.isSendingPanic) Color(0xFF8B0000) else Color(0xFFCC0000),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 18.sp)
                    Text("PÁNICO", fontSize = 7.sp, letterSpacing = 1.sp, lineHeight = 9.sp,
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.trimStart('#')
        val value = clean.toLong(16)
        when (clean.length) {
            6 -> Color(0xFF000000 or value)
            8 -> Color(value)
            else -> Color(0xFFFF4444)
        }
    } catch (_: Exception) { Color(0xFFFF4444) }
}
