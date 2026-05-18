package com.example.fisgon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fisgon.domain.entity.Product
import com.example.fisgon.presentation.map.MapLibreMap
import com.example.fisgon.presentation.panic.PanicViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private fun Double.fmt4(): String {
    val shifted = (this * 10000.0).roundToLong()
    val intPart = shifted / 10000
    val decPart = abs(shifted % 10000).toString().padStart(4, '0')
    return "$intPart.$decPart"
}

private val BgColor    = Color(0xFF090E1C)
private val Teal       = Color(0xFF00C9A0)
private val InputBg    = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)
private val CardBg     = Color(0xFF0F1929)
private val TextMuted  = Color(0xFF5A6A85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    panicViewModel: PanicViewModel,
    onOpenUrl: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val panicState by panicViewModel.uiState.collectAsState()

    // Confirmación al sender cuando presiona SOS
    if (panicState.panicSentConfirmation) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissPanicConfirmation() },
            containerColor = Color(0xFF0A2000),
            title = {
                Text(
                    "ALERTA SOS ENVIADA",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Text(
                    "Tu alerta de pánico fue enviada a los usuarios cercanos.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { panicViewModel.dismissPanicConfirmation() }) {
                    Text("OK", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Error: sin ubicación GPS
    if (panicState.noLocationError) {
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissNoLocationError() },
            containerColor = Color(0xFF1A1000),
            title = {
                Text(
                    "SIN UBICACIÓN",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    "Esperando señal GPS. Activa la ubicación e intenta de nuevo.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { panicViewModel.dismissNoLocationError() }) {
                    Text("OK", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Show incoming panic alert dialog
    panicState.incomingAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { panicViewModel.dismissAlert() },
            containerColor = Color(0xFF1A0000),
            title = {
                Text(
                    "ALERTA SOS CERCANA",
                    color = Color(0xFFFF3333),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Se ha recibido una alerta de pánico en tu zona.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ubicacion: ${alert.latitude.fmt4()}, ${alert.longitude.fmt4()}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { panicViewModel.dismissAlert() }) {
                    Text("ENTENDIDO", color = Color(0xFFFF3333), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .verticalScroll(rememberScrollState())
                .safeContentPadding()
                .padding(horizontal = 20.dp)
        ) {
        Spacer(Modifier.height(20.dp))

        // ── Header ──────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("FISGÓN", color = Teal, fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                Text("SEGURIDAD ACTIVA", color = TextMuted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
            // WS connection status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (panicState.isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        CircleShape
                    )
            )
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Spacer(Modifier.height(16.dp))

        // ── Bienvenida ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A1520), RoundedCornerShape(14.dp))
                .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text("¡Bienvenido!", color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                viewModel.currentUser.anonymousUsername
                    .takeIf { it.isNotBlank() }
                    ?.let { "@$it" }
                    ?: "${viewModel.currentUser.nombre} ${viewModel.currentUser.apellido}",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Text("Tu identidad pública es anónima", color = TextMuted, fontSize = 12.sp)
            Text("Acceso exitoso al sistema", color = Color(0xFF4CAF50), fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        // ── Mapa ───────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MAPA", color = Teal, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                modifier = Modifier.weight(1f))
            panicState.currentLocation?.let { loc ->
                Text(
                    "${loc.latitude.fmt4()}, ${loc.longitude.fmt4()}",
                    color = TextMuted, fontSize = 9.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF0A1520), RoundedCornerShape(14.dp))
                .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            MapLibreMap(modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(24.dp))

        // ── CRUD de Productos ────────────────────────────────────
        Text("GESTIÓN DE PRODUCTOS", color = Teal, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))

        // Formulario
        CrudField("Código del producto", state.formCodigo, viewModel::onCodigoChange)
        Spacer(Modifier.height(10.dp))
        CrudField("Descripción del producto", state.formDescripcion, viewModel::onDescripcionChange)
        Spacer(Modifier.height(10.dp))
        CrudField("Precio del producto", state.formPrecio, viewModel::onPrecioChange,
            keyboard = KeyboardType.Decimal)
        Spacer(Modifier.height(10.dp))
        CrudField("Marca del producto", state.formMarca, viewModel::onMarcaChange)

        Spacer(Modifier.height(20.dp))

        // Mensaje de operación
        state.message?.let { msg ->
            Text(
                text = msg,
                color = if (state.messageIsError) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessage()
            }
        }

        // Botones CRUD
        CrudButton("Registrar Producto", Teal) { viewModel.onRegistrar() }
        Spacer(Modifier.height(8.dp))

        // Buscar con campo de búsqueda
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.weight(1f).height(52.dp),
                placeholder = { Text("Buscar...", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal, unfocusedBorderColor = BorderColor,
                    focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Teal
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = viewModel::onBuscar,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InputBg, contentColor = Color.White)
            ) { Text("Buscar", fontSize = 13.sp) }
        }

        Spacer(Modifier.height(8.dp))
        CrudButton("Modificar Producto", Color(0xFF2196F3)) { viewModel.onModificar() }
        Spacer(Modifier.height(8.dp))
        CrudButton("Eliminar Producto", Color(0xFFFF5252)) { viewModel.onEliminar() }

        Spacer(Modifier.height(24.dp))

        // ── Lista de productos ───────────────────────────────────
        if (state.products.isNotEmpty()) {
            Text("LISTA DE PRODUCTOS (${state.products.size})", color = Teal,
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            state.products.forEach { product ->
                ProductItem(
                    product = product,
                    isSelected = state.selectedProduct?.id == product.id,
                    onClick = { viewModel.onProductSelected(product) }
                )
                Spacer(Modifier.height(6.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin productos registrados", color = TextMuted, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Redes Sociales ───────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Spacer(Modifier.height(20.dp))

        Text("REDES SOCIALES", color = Teal, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "f", label = "Facebook",
                color = Color(0xFF1877F2),
                onClick = { onOpenUrl("https://www.facebook.com") }
            )
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "in", label = "Instagram",
                color = Color(0xFFE4405F),
                onClick = { onOpenUrl("https://www.instagram.com") }
            )
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "▶", label = "YouTube",
                color = Color(0xFFFF0000),
                onClick = { onOpenUrl("https://www.youtube.com") }
            )
        }

        Spacer(Modifier.height(100.dp)) // space for FAB
    } // end Column

    // ── Botón de Pánico (FAB) ────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(bottom = 24.dp),
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
    } // end outer Box
}

// ── Componentes privados ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrudField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label, color = TextMuted, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Teal, unfocusedBorderColor = BorderColor,
            focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            cursorColor = Teal, focusedPlaceholderColor = TextMuted, unfocusedPlaceholderColor = TextMuted
        )
    )
}

@Composable
private fun CrudButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun ProductItem(product: Product, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Teal.copy(alpha = 0.1f) else CardBg,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isSelected) Teal.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.codigo, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(product.descripcion, color = Color.White, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.marca, color = TextMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("S/ ${formatPrice(product.precio)}", color = Color.White,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatPrice(value: Double): String {
    val cents = (value * 100).roundToInt()
    val soles = cents / 100
    val decimals = (cents % 100).toString().padStart(2, '0')
    return "$soles.$decimals"
}

@Composable
private fun SocialButton(
    modifier: Modifier = Modifier,
    letter: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letter, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                lineHeight = 16.sp)
            Text(label, color = Color.White, fontSize = 9.sp, lineHeight = 10.sp)
        }
    }
}
