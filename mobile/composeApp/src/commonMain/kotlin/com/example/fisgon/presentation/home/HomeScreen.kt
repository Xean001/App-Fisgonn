package com.example.fisgon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
private val BgColor     = Color(0xFF090E1C)
private val Teal        = Color(0xFF00C9A0)
private val BorderColor = Color(0xFF1E2D47)
private val InputBg     = Color(0xFF111827)
private val CardBg      = Color(0xFF0F1929)
private val TextMuted   = Color(0xFF5A6A85)
private val TextSoft    = Color(0xFFA8B3C7)
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
    val reportScrollState = rememberScrollState()

    // ── Diálogos de pánico ──────────────────────────────────────
    if (panicState.panicSentConfirmation) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissPanicConfirmation() },
            containerColor = Color(0xFF0A2000),
            title = { Text("SOS enviado", color = Color(0xFF4CAF50),
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text = { Text("Avisamos a usuarios cercanos para que estén atentos a tu zona.",
                color = Color.White, fontSize = 14.sp) },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissPanicConfirmation() }) {
                Text("Entendido", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) } }
        )
    }

    if (panicState.noLocationError) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissNoLocationError() },
            containerColor = Color(0xFF1A1000),
            title = { Text("Sin ubicación", color = Color(0xFFFF9800),
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text = { Text("Necesitamos señal GPS para enviar una alerta precisa.",
                color = Color.White, fontSize = 14.sp) },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissNoLocationError() }) {
                Text("Entendido", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold) } }
        )
    }

    panicState.incomingAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissAlert() },
            containerColor = Color(0xFF1A0000),
            title = { Text("SOS cerca de ti", color = RedAlert,
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Alguien activó una alerta de pánico en tu zona.", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Revisa el mapa y mantente alerta.",
                        color = Color(0xFFAAAAAA), fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { panicViewModel.dismissAlert() }) {
                Text("Entendido", color = RedAlert, fontWeight = FontWeight.Bold) } }
        )
    }

    if (state.markerAdded) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMarkerAdded() },
            containerColor = Color(0xFF0A1520),
            title = { Text("Incidente reportado", color = RedAlert,
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Tu reporte quedó marcado para alertar a personas cercanas.",
                        color = Color.White, fontSize = 14.sp)
                    if (state.reportError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("Sin conexión: se guardó solo en este dispositivo.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.dismissMarkerAdded() }) {
                Text("Listo", color = Teal, fontWeight = FontWeight.Bold) } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .safeContentPadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .background(Color(0xFF0A1520))
                    .border(1.dp, Teal.copy(alpha = 0.2f))
            ) {
                MapLibreMap(
                    modifier = Modifier.fillMaxSize(),
                    latitude = panicState.currentLocation?.latitude,
                    longitude = panicState.currentLocation?.longitude,
                    markers = state.markers
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "FISGÓN",
                                color = Teal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp
                            )
                            Text("Protección en tiempo real", color = TextMuted, fontSize = 10.sp)
                        }
                        ConnectionStatus(isConnected = panicState.isConnected)
                    }

                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderColor.copy(alpha = 0.65f))
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "TU UBICACIÓN",
                            color = Teal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill(
                            text = if (panicState.currentLocation != null) "GPS activo" else "Buscando GPS",
                            color = if (panicState.currentLocation != null) Teal else Color(0xFFFFB74D)
                        )
                    }
                }

                MapStatusOverlay(
                    hasLocation = panicState.currentLocation != null,
                    reportCount = state.markers.size,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardBg, RoundedCornerShape(14.dp))
                        .border(1.dp, RedAlert.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .verticalScroll(reportScrollState)
                        .imePadding()
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "REPORTE RÁPIDO",
                                color = RedAlert,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Marca una situación visible en tu ubicación.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            if (state.markers.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                StatusPill(text = "${state.markers.size} en mapa", color = RedAlert)
                            }
                        }
                        Button(
                            onClick = { panicViewModel.onPanicButtonPressed() },
                            modifier = Modifier.size(96.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (panicState.isSendingPanic) Color(0xFF8B0000) else Color(0xFFCC0000),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp)
                                Text(
                                    if (panicState.isSendingPanic) "ENVIANDO" else "PÁNICO",
                                    fontSize = 7.sp,
                                    letterSpacing = 1.sp,
                                    lineHeight = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = state.categoryMenuExpanded,
                        onExpandedChange = { viewModel.onCategoryMenuToggle() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.selectedCategory?.localizedName() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            placeholder = { Text("Selecciona una categoría", color = TextMuted, fontSize = 13.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.categoryMenuExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedAlert,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = InputBg,
                                unfocusedContainerColor = InputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = state.categoryMenuExpanded,
                            onDismissRequest = { viewModel.onCategoryMenuDismiss() },
                            modifier = Modifier.background(Color(0xFF0F1929))
                        ) {
                            state.categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.localizedName(), color = Color.White, fontSize = 13.sp) },
                                    onClick = { viewModel.onCategorySelected(cat) },
                                    leadingIcon = {
                                        Box(
                                            Modifier
                                                .size(10.dp)
                                                .background(parseHexColor(cat.color), CircleShape)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = state.formTitle,
                        onValueChange = viewModel::onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej. Robo cerca de la avenida", color = TextMuted, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAlert,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = InputBg,
                            unfocusedContainerColor = InputBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = RedAlert
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = state.formDescription,
                        onValueChange = viewModel::onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Describe lo ocurrido", color = TextMuted, fontSize = 13.sp) },
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAlert,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = InputBg,
                            unfocusedContainerColor = InputBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = RedAlert
                        )
                    )

                    if (state.noGpsError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Sin señal GPS. Activa la ubicación para reportar con precisión.",
                            color = Color(0xFFFF9800),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

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
                            containerColor = RedAlert,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedAlert.copy(alpha = 0.6f))
                    ) {
                        Text("Enviar reporte", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapStatusOverlay(
    hasLocation: Boolean,
    reportCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xDD0A1520), RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            if (hasLocation) "Estás aquí" else "Buscando tu ubicación",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (reportCount == 0) "Sin reportes marcados" else "$reportCount reporte${if (reportCount > 1) "s" else ""} en el mapa",
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ConnectionStatus(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(CardBg, RoundedCornerShape(50))
            .border(1.dp, BorderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            Modifier.size(7.dp).background(
                if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
                CircleShape
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (isConnected) "Conectado" else "Sin conexión",
            color = TextSoft,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text, color = TextSoft, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
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
